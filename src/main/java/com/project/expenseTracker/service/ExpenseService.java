package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.CategoryDto;
import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.dto.MonthDto;
import com.project.expenseTracker.dto.ExpenseFeedDto;
import com.project.expenseTracker.dto.ExpenseFeedResponseDto;
import com.project.expenseTracker.dto.ExpenseSearchResponseDto;
import com.project.expenseTracker.entity.*;
import com.project.expenseTracker.exception.InvalidRequestException;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.project.expenseTracker.service.MonthService.getMonthName;
import static com.project.expenseTracker.service.MonthService.getMonthYear;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final MonthRepository monthRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MonthService monthService;
    private final CategoryService categoryService;
    private final EventRepository eventRepository;

    public long createExpense(ExpenseDto expenseDto) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentPrincipalName = authentication.getName();
//        System.out.println("user " + currentPrincipalName);

        User user = userRepository.findById(expenseDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

//        String monthName = expenseDto.getMonthName().split(",")[0];
//        int year = Integer.parseInt(expenseDto.getMonthName().split(",")[1]);
        Optional<Month> monthOptional = monthRepository.findByNameAndYearAndUserId(getMonthName(expenseDto.getMonthName())
                , getMonthYear(expenseDto.getMonthName()), user.getId());
//                .orElseThrow(
//                () -> new ResourceNotFoundException("Month not found")
//        );
        Month month;
        if (monthOptional.isEmpty()) {
            Long monthId = monthService.createMonth(MonthDto.builder()
                    .name(expenseDto.getMonthName())
                    .userId(user.getId())
                    .build());
            month = monthRepository.findById(monthId).orElseThrow(
                    () -> new InvalidRequestException("Couldn't create new month")
            );
        } else {
            month = monthOptional.get();
        }

        Optional<Category> category = Optional.empty();
        if (!expenseDto.getCategoryName().isEmpty()) {
            category = categoryRepository.findByNameAndMonthIdAndUserId(expenseDto.getCategoryName(), month.getId(), user.getId());
            if(category.isEmpty()){
                Long categoryId = categoryService.createCategory(CategoryDto.builder()
                        .name(expenseDto.getCategoryName())
                        .userId(user.getId())
                        .monthName(expenseDto.getMonthName())
                        .build());
                category = categoryRepository.findById(categoryId);
            }
        }

        Optional<Event> event = eventRepository.findByName(expenseDto.getEventName());

        Expense expense = mapDtoToEntity(null, expenseDto, month, category.orElse(null), user, event.orElse(null));
        return expenseRepository.save(expense).getId();
    }

    public ExpenseDto updateExpense(ExpenseDto expenseDto, Long expenseId){
        Expense existingExpense = expenseRepository.findById(expenseId).orElseThrow(
                () -> new ResourceNotFoundException("Expense not found"));
//        Month month = existingExpense.getMonth();
        Expense expense = expenseRepository.save(mapDtoToEntity(existingExpense, expenseDto, null, null, null, null));
        return mapEntityToDTo(expense);
    }

    public ExpenseDto getExpenseById(Long id){
        Expense expense = expenseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Expense not found"));
        return mapEntityToDTo(expense);
    }

    public List<ExpenseDto> getExpenses(Long userId, String monthName, String categoryName, String expenseName){
//        String name = null;
//        Integer year = null;
//        if(monthName != null){
//            name = monthName.split(",")[0];
//            year = Integer.parseInt(monthName.split(",")[1]);
//        }

        List<Expense> expenses = expenseRepository.findByFilters(userId, getMonthName(monthName), getMonthYear(monthName), categoryName, expenseName);
        return expenses.stream().map(ExpenseService::mapEntityToDTo).sorted(Comparator.comparing(ExpenseDto::getDate)).toList();
    }

    /**
     * Unified expense search endpoint with filtering, pagination, and sorting
     * 
     * @param userId User ID (required)
     * @param monthName Optional month filter in format "MonthName,year" (e.g., "March,2026")
     * @param categoryName Optional category filter
     * @param expenseName Optional expense description search
     * @param fromDate Optional start date filter (yyyy-MM-dd)
     * @param toDate Optional end date filter (yyyy-MM-dd)
     * @param limit Page size (default 20)
     * @param cursor Optional cursor for pagination - date of last item from previous page
     * @param sortOrder Sort order: "asc" or "desc" (default "desc")
     * @return ExpenseSearchResponseDto with items, nextCursor, hasMore, and totalItems
     */
    public ExpenseSearchResponseDto searchExpenses(Long userId, String monthName, String categoryName, 
                                                   String expenseName, String fromDate, String toDate,
                                                   Integer limit, String cursor, String sortOrder) {
        // Validate user exists
        userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        // Set default limit
        if (limit == null || limit <= 0) {
            limit = 20;
        }

        // Validate sort order
        if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            sortOrder = "desc";
        }

        // Get month ID if monthName is provided
        Long monthId = null;
        if (monthName != null && !monthName.isEmpty()) {
            Optional<Month> monthOptional = monthRepository.findByNameAndYearAndUserId(
                    getMonthName(monthName), getMonthYear(monthName), userId);
            if (monthOptional.isPresent()) {
                monthId = monthOptional.get().getId();
            }
        }

        // Fetch expenses with search criteria
        List<Expense> allExpenses = expenseRepository.findSearchWithFilters(
                userId, monthId, categoryName, expenseName, fromDate, toDate
        );

        // Apply cursor-based pagination and sorting
        boolean isDescending = sortOrder.equalsIgnoreCase("desc");
        
        // Filter by cursor if provided
        List<Expense> filteredExpenses;
        if (cursor != null && !cursor.isEmpty()) {
            if (isDescending) {
                // For descending order, get expenses before the cursor date
                filteredExpenses = allExpenses.stream()
                        .filter(e -> e.getDate().compareTo(cursor) < 0)
                        .toList();
            } else {
                // For ascending order, get expenses after the cursor date
                filteredExpenses = allExpenses.stream()
                        .filter(e -> e.getDate().compareTo(cursor) > 0)
                        .toList();
            }
        } else {
            filteredExpenses = allExpenses;
        }

        // Check if there are more items
        boolean hasMore = filteredExpenses.size() > limit;
        
        // Convert to DTOs and apply limit
        List<ExpenseDto> items = filteredExpenses.stream()
                .limit(limit)
                .map(ExpenseService::mapEntityToDTo)
                .toList();

        // Calculate next cursor (date of last item if hasMore is true)
        String nextCursor = null;
        if (hasMore && !items.isEmpty()) {
            nextCursor = items.get(items.size() - 1).getDate();
        }

        // Get total count of matching items
        Long totalItems = expenseRepository.countSearchResults(userId, monthId, categoryName, expenseName, fromDate, toDate);

        return ExpenseSearchResponseDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .totalItems(totalItems)
                .build();
    }

//    public List<ExpenseDto> getExpenseByCategory(Long categoryId){
//        List<Expense> expenses = expenseRepository.findByCategoryId(categoryId);
//        return expenses.stream().map(ExpenseService::mapEntityToDTo).toList();
//    }

    public long deleteExpenseById(Long id){
        expenseRepository.deleteById(id);
        return id;
    }

    private Expense mapDtoToEntity(Expense existingExpense, ExpenseDto expenseDto, Month month, Category category, User user, Event event) {
        if(existingExpense != null){
            existingExpense.setDescription(expenseDto.getDescription());
            existingExpense.setAmount(expenseDto.getAmount());
            existingExpense.setDate(expenseDto.getDate());
            return existingExpense;
        }
        return Expense.builder()
                .amount(expenseDto.getAmount())
                .description(expenseDto.getDescription())
                .date(expenseDto.getDate())
                .month(month)
                .category(category)
                .event(event)
                .user(user)
                .build();
    }

    public static ExpenseDto mapEntityToDTo(Expense expense){
        return ExpenseDto.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .date(expense.getDate())
                .monthName(expense.getMonth().getName() +","+ expense.getMonth().getYear())
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .userId(expense.getUser().getId())
                .build();
    }

    public List<ExpenseDto> getTop5ByMonth(Long userId, String monthName){
        Month month = monthRepository.findByNameAndYearAndUserId(getMonthName(monthName),
                getMonthYear(monthName), userId).orElseThrow(
                () -> new ResourceNotFoundException("Month not found"));

        return month.getExpenses().stream().sorted(Comparator.comparing(Expense::getAmount).reversed()).limit(5).
    map(ExpenseService::mapEntityToDTo).toList();
    }

    /**
     * Get expense feed for a user with optional month filter
     * Returns expenses sorted by date (most recent first) with limit
     * 
     * @param userId User ID
     * @param monthName Optional month name in format "MonthName,year"
     * @param limit Maximum number of expenses to return (default 20)
     * @return List of expenses sorted by date descending
     */
    public List<ExpenseDto> getExpenseFeed(Long userId, String monthName, Integer limit){
        // Validate user exists
        userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        List<Expense> expenses;
        if(monthName != null && !monthName.isEmpty()){
            // Get expenses for specific month
            Month month = monthRepository.findByNameAndYearAndUserId(getMonthName(monthName),
                    getMonthYear(monthName), userId).orElseThrow(
                    () -> new ResourceNotFoundException("Month not found")
            );
            expenses = month.getExpenses() != null ? month.getExpenses() : new java.util.ArrayList<>();
        } else {
            // Get all expenses for user
            expenses = expenseRepository.findByFilters(userId, null, null, null, null);
        }

        // Sort by date descending (most recent first) and apply limit
        return expenses.stream()
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .limit(limit)
                .map(ExpenseService::mapEntityToDTo)
                .toList();
    }

    /**
     * Get expense feed with cursor-based pagination for infinite scrolling
     * 
     * @param userId User ID (required)
     * @param monthName Optional month name in format "MonthName,year"
     * @param cursor Optional cursor (date string) for pagination, expenses before this date
     * @param limit Number of items to return (default 20)
     * @param fromDate Optional start date filter
     * @param toDate Optional end date filter
     * @param categoryId Optional category filter
     * @param eventId Optional event filter
     * @return ExpenseFeedResponseDto with items and next cursor
     */
    public ExpenseFeedResponseDto getExpenseFeedWithCursor(Long userId, String monthName, String cursor, 
                                                           Integer limit, String fromDate, String toDate, 
                                                           Long categoryId, Long eventId) {
        // Validate user exists
        userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        // Set default limit
        if (limit == null || limit <= 0) {
            limit = 20;
        }

        // Get month ID if monthName is provided
        Long monthId = null;
        if (monthName != null && !monthName.isEmpty()) {
            Month month = monthRepository.findByNameAndYearAndUserId(getMonthName(monthName),
                    getMonthYear(monthName), userId).orElseThrow(
                    () -> new ResourceNotFoundException("Month not found")
            );
            monthId = month.getId();
        }

        // Fetch expenses with cursor pagination (limit + 1 to detect if there are more items)
        List<Expense> expenses = expenseRepository.findFeedWithCursor(
                userId, monthId, cursor, fromDate, toDate, categoryId, eventId
        );

        // Check if there are more items
        boolean hasMore = expenses.size() > limit;
        
        // Convert to DTOs and apply limit
        List<ExpenseFeedDto> feedItems = expenses.stream()
                .limit(limit)
                .map(this::mapEntityToFeedDto)
                .toList();

        // Calculate next cursor (date of last item if hasMore is true)
        String nextCursor = null;
        if (hasMore && !feedItems.isEmpty()) {
            nextCursor = feedItems.get(feedItems.size() - 1).getDate();
        }

        return ExpenseFeedResponseDto.builder()
                .items(feedItems)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    /**
     * Map Expense entity to ExpenseFeedDto
     */
    private ExpenseFeedDto mapEntityToFeedDto(Expense expense) {
        return ExpenseFeedDto.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .monthName(expense.getMonth().getName() + "," + expense.getMonth().getYear())
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .eventName(expense.getEvent() != null ? expense.getEvent().getName() : null)
                .build();
    }
}
