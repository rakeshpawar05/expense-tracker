package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.dto.SubscriptionDto;
import com.project.expenseTracker.dto.SubscriptionSummaryDto;
import com.project.expenseTracker.entity.Subscription;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.InvalidRequestException;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.SubscriptionRepository;
import com.project.expenseTracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ExpenseService expenseService;

    public SubscriptionSummaryDto getSubscriptionSummary(Long userId, YearMonth targetYm) {
        LocalDate today = LocalDate.now();
        YearMonth ym = targetYm != null ? targetYm : YearMonth.from(today);

        List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByBillingDayAsc(userId);

        double totalMonthlyCommitment = 0.0;
        int activeCount = 0;
        int pausedCount = 0;
        int dueSoonCount = 0;

        List<SubscriptionDto> dtos = new ArrayList<>();

        for (Subscription sub : subscriptions) {
            boolean isActive = "ACTIVE".equalsIgnoreCase(sub.getStatus());
            if (isActive) {
                activeCount++;
                double monthlyEquivalent = calculateMonthlyEquivalent(sub.getAmount(), sub.getBillingCycle());
                totalMonthlyCommitment += monthlyEquivalent;
            } else {
                pausedCount++;
            }

            int billingDay = sub.getBillingDay() != null ? sub.getBillingDay() : 1;
            int safeDay = Math.min(billingDay, ym.lengthOfMonth());
            LocalDate dueDateThisMonth = ym.atDay(safeDay);

            long daysUntil = ChronoUnit.DAYS.between(today, dueDateThisMonth);
            boolean isLoggedThisMonth = sub.getLastBilledDate() != null && sub.getLastBilledDate().startsWith(ym.toString());
            boolean isDueSoon = isActive && !isLoggedThisMonth && daysUntil >= 0 && daysUntil <= 7;
            boolean isOverdue = isActive && !isLoggedThisMonth && daysUntil < 0;

            if (isDueSoon) {
                dueSoonCount++;
            }

            SubscriptionDto dto = SubscriptionDto.builder()
                    .id(sub.getId())
                    .name(sub.getName())
                    .amount(sub.getAmount())
                    .billingCycle(sub.getBillingCycle() != null ? sub.getBillingCycle() : "MONTHLY")
                    .billingDay(billingDay)
                    .categoryName(sub.getCategoryName())
                    .paymentMethod(sub.getPaymentMethod())
                    .status(sub.getStatus() != null ? sub.getStatus() : "ACTIVE")
                    .lastBilledDate(sub.getLastBilledDate())
                    .nextDueDate(dueDateThisMonth.toString())
                    .notes(sub.getNotes())
                    .userId(userId)
                    .daysUntilDue((int) daysUntil)
                    .isDueSoon(isDueSoon)
                    .isOverdue(isOverdue)
                    .isLoggedThisMonth(isLoggedThisMonth)
                    .build();

            dtos.add(dto);
        }

        double totalAnnualCommitment = totalMonthlyCommitment * 12.0;

        return SubscriptionSummaryDto.builder()
                .totalMonthlyCommitment((double) Math.round(totalMonthlyCommitment))
                .totalAnnualCommitment((double) Math.round(totalAnnualCommitment))
                .activeCount(activeCount)
                .pausedCount(pausedCount)
                .dueSoonCount(dueSoonCount)
                .subscriptions(dtos)
                .build();
    }

    @Transactional
    public SubscriptionDto createSubscription(SubscriptionDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        Subscription subscription = Subscription.builder()
                .name(dto.getName().trim())
                .amount(dto.getAmount())
                .billingCycle(dto.getBillingCycle() != null ? dto.getBillingCycle().toUpperCase() : "MONTHLY")
                .billingDay(dto.getBillingDay() != null ? Math.max(1, Math.min(31, dto.getBillingDay())) : 1)
                .categoryName(dto.getCategoryName() != null ? dto.getCategoryName().trim() : "Bills")
                .paymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod().trim() : "AutoPay")
                .status(dto.getStatus() != null ? dto.getStatus().toUpperCase() : "ACTIVE")
                .notes(dto.getNotes())
                .user(user)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        return mapEntityToDto(saved);
    }

    @Transactional
    public SubscriptionDto updateSubscription(Long id, SubscriptionDto dto) {
        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        subscription.setName(dto.getName().trim());
        subscription.setAmount(dto.getAmount());
        if (dto.getBillingCycle() != null) {
            subscription.setBillingCycle(dto.getBillingCycle().toUpperCase());
        }
        if (dto.getBillingDay() != null) {
            subscription.setBillingDay(Math.max(1, Math.min(31, dto.getBillingDay())));
        }
        if (dto.getCategoryName() != null) {
            subscription.setCategoryName(dto.getCategoryName().trim());
        }
        if (dto.getPaymentMethod() != null) {
            subscription.setPaymentMethod(dto.getPaymentMethod().trim());
        }
        if (dto.getStatus() != null) {
            subscription.setStatus(dto.getStatus().toUpperCase());
        }
        subscription.setNotes(dto.getNotes());

        Subscription saved = subscriptionRepository.save(subscription);
        return mapEntityToDto(saved);
    }

    @Transactional
    public void deleteSubscription(Long id, Long userId) {
        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        subscriptionRepository.delete(subscription);
    }

    @Transactional
    public SubscriptionDto toggleStatus(Long id, Long userId) {
        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        String newStatus = "ACTIVE".equalsIgnoreCase(subscription.getStatus()) ? "PAUSED" : "ACTIVE";
        subscription.setStatus(newStatus);
        Subscription saved = subscriptionRepository.save(subscription);
        return mapEntityToDto(saved);
    }

    @Transactional
    public long logSubscriptionExpense(Long id, Long userId, YearMonth requestedYm) {
        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        YearMonth ym = requestedYm != null ? requestedYm : YearMonth.now();
        int billingDay = subscription.getBillingDay() != null ? subscription.getBillingDay() : LocalDate.now().getDayOfMonth();
        int safeDay = Math.min(billingDay, ym.lengthOfMonth());
        String expenseDate = ym.atDay(safeDay).toString();

        ExpenseDto expenseDto = ExpenseDto.builder()
                .description(subscription.getName() + " (Recurring)")
                .amount(subscription.getAmount())
                .date(expenseDate)
                .monthName(ym.getMonth().name())
                .yearMonth(ym)
                .categoryName(subscription.getCategoryName() != null ? subscription.getCategoryName() : "Bills")
                .userId(userId)
                .build();

        long expenseId = expenseService.createExpense(expenseDto);

        // Update last billed date on subscription
        subscription.setLastBilledDate(expenseDate);
        subscriptionRepository.save(subscription);

        log.info("Logged recurring subscription '{}' as expense id: {} for cycle: {}", subscription.getName(), expenseId, ym);
        return expenseId;
    }

    private double calculateMonthlyEquivalent(int amount, String cycle) {
        if (cycle == null) return amount;
        return switch (cycle.toUpperCase()) {
            case "YEARLY", "ANNUAL" -> amount / 12.0;
            case "QUARTERLY" -> amount / 3.0;
            case "WEEKLY" -> amount * 4.33;
            default -> amount;
        };
    }

    private SubscriptionDto mapEntityToDto(Subscription sub) {
        return SubscriptionDto.builder()
                .id(sub.getId())
                .name(sub.getName())
                .amount(sub.getAmount())
                .billingCycle(sub.getBillingCycle())
                .billingDay(sub.getBillingDay())
                .categoryName(sub.getCategoryName())
                .paymentMethod(sub.getPaymentMethod())
                .status(sub.getStatus())
                .lastBilledDate(sub.getLastBilledDate())
                .nextDueDate(sub.getNextDueDate())
                .notes(sub.getNotes())
                .userId(sub.getUser() != null ? sub.getUser().getId() : null)
                .build();
    }
}
