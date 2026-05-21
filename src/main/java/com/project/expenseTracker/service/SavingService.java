package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.CategoryDto;
import com.project.expenseTracker.dto.SavingDto;
import com.project.expenseTracker.dto.MonthDto;
import com.project.expenseTracker.entity.*;
import com.project.expenseTracker.exception.InvalidRequestException;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SavingService {

    private final SavingRepository savingRepository;
    private final MonthRepository monthRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MonthService monthService;
    private final CategoryService categoryService;
    private final EventRepository eventRepository;

    public long createSaving(SavingDto savingDto) {

        User user = userRepository.findById(savingDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        Optional<Month> monthOptional = Optional.ofNullable(monthService.getMonthByUserIdAndYearMonth(user.getId(), savingDto.getYearMonth()));
        Month month;
        if (monthOptional.isEmpty()) {
            Long monthId = monthService.createMonth(MonthDto.builder()
                    .name(savingDto.getMonthName())
                    .userId(user.getId())
                    .yearMonth(savingDto.getYearMonth())
                    .earning(0.0) // Default earning
                    .build());
            month = monthRepository.findById(monthId).orElseThrow(
                    () -> new InvalidRequestException("Couldn't create new month")
            );
        } else {
            month = monthOptional.get();
        }

        Optional<Category> category = Optional.empty();
        if (!savingDto.getCategoryName().isEmpty()) {
            category = categoryRepository.findByNameAndMonthIdAndUserId(savingDto.getCategoryName(), month.getId(), user.getId());
            if(category.isEmpty()){
                Long categoryId = categoryService.createCategory(CategoryDto.builder()
                        .name(savingDto.getCategoryName())
                        .userId(user.getId())
                        .monthName(savingDto.getMonthName())
                        .yearMonth(savingDto.getYearMonth())
                        .build());
                category = categoryRepository.findById(categoryId);
            }
        }

        Optional<Event> event = eventRepository.findByName(savingDto.getEventName());

        Saving saving = mapDtoToEntity(null, savingDto, month, category.orElse(null), user, event.orElse(null));
        return savingRepository.save(saving).getId();
    }

    public SavingDto updateSaving(SavingDto savingDto, Long savingId){
        Saving existingSaving = savingRepository.findById(savingId).orElseThrow(
                () -> new ResourceNotFoundException("Saving not found"));
        Saving saving = savingRepository.save(mapDtoToEntity(existingSaving, savingDto, null, null, null, null));
        return mapEntityToDTo(saving);
    }

    public SavingDto getSavingById(Long id){
        Saving saving = savingRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Saving not found"));
        return mapEntityToDTo(saving);
    }

    public List<SavingDto> getSavings(Long userId, YearMonth yearMonth, String categoryName, String savingName){
//        String name = null;
//        Integer year = null;
//        if(monthName != null){
//            name = monthName.split(",")[0];
//            year = Integer.parseInt(monthName.split(",")[1]);
//        }

        List<Saving> savings = savingRepository.findByFilters(userId, yearMonth == null ? null : yearMonth.getMonthValue(),
                yearMonth == null ? null : yearMonth.getYear(), categoryName, savingName);
        return savings.stream().map(SavingService::mapEntityToDTo).sorted(Comparator.comparing(SavingDto::getDate)).toList();
    }

//    public List<SavingDto> getSavingByCategory(Long categoryId){
//        List<Saving> savings = savingRepository.findByCategoryId(categoryId);
//        return savings.stream().map(SavingService::mapEntityToDTo).toList();
//    }

    public long deleteSavingById(Long id){
        savingRepository.deleteById(id);
        return id;
    }

    private Saving mapDtoToEntity(Saving existingSaving, SavingDto savingDto, Month month, Category category, User user, Event event) {
        if(existingSaving != null){
            existingSaving.setDescription(savingDto.getDescription());
            existingSaving.setAmount(savingDto.getAmount());
            existingSaving.setDate(savingDto.getDate());
            return existingSaving;
        }
        return Saving.builder()
                .amount(savingDto.getAmount())
                .description(savingDto.getDescription())
                .date(savingDto.getDate())
                .month(month)
                .category(category)
                .event(event)
                .user(user)
                .build();
    }

    public static SavingDto mapEntityToDTo(Saving saving){
        return SavingDto.builder()
                .id(saving.getId())
                .amount(saving.getAmount())
                .description(saving.getDescription())
                .date(saving.getDate())
                .monthName(saving.getMonth().getName() +","+ saving.getMonth().getYear())
                .yearMonth(YearMonth.of(saving.getMonth().getYearNum(), saving.getMonth().getMonthNum()))
                .eventName(saving.getEvent() != null ? saving.getEvent().getName() : null)
                .categoryName(saving.getCategory() != null ? saving.getCategory().getName() : null)
                .userId(saving.getUser().getId())
                .build();
    }

    public List<SavingDto> getTop5ByMonth(Long userId, YearMonth yearMonth){
        Month month = monthService.getMonthByUserIdAndYearMonth(userId, yearMonth);
        return month.getSavings().stream().sorted(Comparator.comparing(Saving::getAmount).reversed()).limit(5).
    map(SavingService::mapEntityToDTo).toList();
    }
}
