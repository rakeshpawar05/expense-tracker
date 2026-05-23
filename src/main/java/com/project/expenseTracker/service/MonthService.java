package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.MonthDto;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.DuplicateResourceException;
import com.project.expenseTracker.exception.InvalidRequestException;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.CategoryRepository;
import com.project.expenseTracker.repository.ExpenseRepository;
import com.project.expenseTracker.repository.MonthRepository;
import com.project.expenseTracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class MonthService {

    private final List<String> monthList = Arrays.asList("January", "February", "March", "April",
            "May", "June", "July", "August", "September",
            "October", "November", "December");

    private final MonthRepository monthRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public Long createMonth(MonthDto monthDto){
        User user = userRepository.findById(monthDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Optional<Month> existingMonth = monthRepository.findByMonthNumAndYearNumAndUserId(monthDto.getYearMonth().getMonthValue(),
                monthDto.getYearMonth().getYear(), user.getId());
        if(existingMonth.isPresent()){
            throw new DuplicateResourceException("Month already exist");
        }
        Month month = mapDtoToEntity(null, monthDto, user);
        return monthRepository.save(month).getId();
    }


    public static String getMonthName(String name) {
        return name != null ? name.split(",")[0] : null;
    }

    public MonthDto updateMonth(long monthId, MonthDto monthDto){
        Optional<Month> existingMonth = monthRepository.findById(monthId);
        if(existingMonth.isEmpty()){
            throw new ResourceNotFoundException("Month not found");
        } else if(!existingMonth.get().getUser().getId().equals(monthDto.getUserId())){
            throw  new InvalidRequestException("Month doesn't belong to user");
        }
        Month month = monthRepository.save(mapDtoToEntity(existingMonth.get(), monthDto, null));
        return mapEntityToDto(month);
    }

    public MonthDto getMonthById(Long id){
        Month month = monthRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Month not found"));
        return mapEntityToDto(month);
    }

    public List<MonthDto> getMonths(Long userId, YearMonth yearMonth){
        List<Month> months = monthRepository.findByFilters(userId, yearMonth != null ?yearMonth.getMonthValue(): null,
                yearMonth!= null ?yearMonth.getYear(): null);
        return months.stream().map(MonthService::mapEntityToDto).toList();
    }

    @Transactional
    public Long deleteMonthById(Long id){
        categoryRepository.deleteByMonthId(id);
        monthRepository.deleteById(id);
        return id;
    }


    private Month mapDtoToEntity(Month existingMonth, MonthDto monthDto, User user){
        String name = getMonthName(monthDto.getName());
        int year = monthDto.getYearMonth().getYear();
        if(existingMonth != null){
            existingMonth.setName(name);
            existingMonth.setYear(year);
            existingMonth.setYearNum(monthDto.getYearMonth().getYear());
            existingMonth.setMonthNum(monthDto.getYearMonth().getMonthValue());
            existingMonth.setEarning(monthDto.getEarning());
            return existingMonth;
        } else {
        return Month.builder()
                .name(name)
                .year(year)
                .yearNum(monthDto.getYearMonth().getYear())
                .monthNum(monthDto.getYearMonth().getMonthValue())
                .earning(monthDto.getEarning())
                .user(user)
                .build();
        }
    }

    public static MonthDto mapEntityToDto(Month month){
        return MonthDto.builder()
                .id(month.getId())
                .name(month.getName()+","+month.getYear())
                .yearMonth(YearMonth.of(month.getYearNum(), month.getMonthNum()))
                .earning(month.getEarning())
                .userId(month.getUser().getId())
                .categories(month.getCategories().stream().map(CategoryService::mapEntityToDto).toList())
                .expenses(month.getExpenses().stream().map(ExpenseService::mapEntityToDTo).toList())
                .savings(month.getSavings().stream().map(SavingService::mapEntityToDTo).toList())
                .build();
    }

    public List<String> getMonthNamesByUserId(Long userId) {
        List<Month> months = monthRepository.findByUserId(userId);
        return months.stream().map(month -> YearMonth.of(month.getYearNum(), month.getMonthNum()).toString()).toList();
//        return months.stream().sorted(Comparator.comparingInt(
//                month -> java.time.Month.valueOf(month.getName().toUpperCase()).getValue()))
//                .map(month -> month.getName() + "," + month.getYear()).toList();
    }

    public Optional<Month> getMonthByUserIdAndYearMonth(Long userId, YearMonth yearMonth){
        return monthRepository.findByMonthNumAndYearNumAndUserId(yearMonth.getMonthValue(), yearMonth.getYear(), userId);
    }

}
