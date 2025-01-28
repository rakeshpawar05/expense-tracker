package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.MonthDto;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.DuplicateResourceException;
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
        Optional<Month> existingMonth = monthRepository.findByNameAndYear(monthDto.getName().split(",")[0],
                Integer.parseInt(monthDto.getName().split(",")[1]));
        if(existingMonth.isPresent()){
            throw new DuplicateResourceException("Month already exist");
        }
        Month month = mapDtoToEntity(null, monthDto, user);
        return monthRepository.save(month).getId();
    }

    public MonthDto updateMonth(long monthId, MonthDto monthDto){
        Optional<Month> existingMonth = monthRepository.findById(monthId);
        if(existingMonth.isEmpty()){
            throw new ResourceNotFoundException("Month not found");
        }
        Month month = monthRepository.save(mapDtoToEntity(existingMonth.get(), monthDto, null));
        return mapEntityToDto(month);
    }

    public MonthDto getMonthById(Long id){
        Month month = monthRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Month not found"));
        return mapEntityToDto(month);
    }

    public List<MonthDto> getMonthByUserId(Long userId){
        List<Month> months = monthRepository.findByUserId(userId);
        return months.stream().map(MonthService::mapEntityToDto).toList();
    }

    public int getAmountForMonth(Long monthId){
        List<Expense> expenses = expenseRepository.findByMonthId(monthId);
        return expenses.stream().mapToInt(Expense::getAmount).sum();
    }

    @Transactional
    public Long deleteMonthById(Long id){
        categoryRepository.deleteByMonthId(id);
        monthRepository.deleteById(id);
        return id;
    }


    private Month mapDtoToEntity(Month existingMonth, MonthDto monthDto, User user){
        String name = monthDto.getName().split(",")[0];
        int year = Integer.parseInt(monthDto.getName().split(",")[1]);
        if(existingMonth != null){
            existingMonth.setName(name);
            existingMonth.setYear(year);
            existingMonth.setEarning(monthDto.getEarning());
            return existingMonth;
        } else {
        return Month.builder()
                .name(name)
                .year(year)
                .earning(monthDto.getEarning())
                .user(user)
                .build();
        }
    }

    private static MonthDto mapEntityToDto(Month month){
        return MonthDto.builder()
                .id(month.getId())
                .name(month.getName()+","+month.getYear())
//                .year(month.getYear())
                .earning(month.getEarning())
                .userId(month.getUser().getId())
                .categories(month.getCategories().stream().map(CategoryService::mapEntityToDto).toList())
                .expenses(month.getExpenses().stream().map(ExpenseService::mapEntityToDTo).toList())
                .build();
    }

    public List<String> getMonthNamesByUserId(Long userId) {
        List<Month> months = monthRepository.findByUserId(userId);
        return months.stream().sorted(Comparator.comparingInt(
                month -> java.time.Month.valueOf(month.getName().toUpperCase()).getValue()))
                .map(month -> month.getName() + "," + month.getYear()).toList();
    }

    public MonthDto getMonthByName(String name) {
        Month month = monthRepository.findByNameAndYear(name.split(",")[0],
                Integer.parseInt(name.split(",")[1])).orElseThrow(
                () -> new ResourceNotFoundException("Month not found"));
        return mapEntityToDto(month);
    }

}
