package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.MonthDto;
import com.project.expenseTracker.entity.Category;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.CategoryRepository;
import com.project.expenseTracker.repository.MonthRepository;
import com.project.expenseTracker.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonthService {

    private final MonthRepository monthRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public Long createMonth(MonthDto monthDto){
        User user = userRepository.findById(monthDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Month month = createMonth(null, monthDto, user);
        return monthRepository.save(month).getId();
    }

    public MonthDto updateMonth(long monthId, MonthDto monthDto){
        Optional<Month> existingMonth = monthRepository.findById(monthId);
        if(existingMonth.isEmpty()){
            throw new ResourceNotFoundException("Month not found");
        }
        Month month = monthRepository.save(createMonth(existingMonth.get(), monthDto, null));
        return modelMapper.map(month, MonthDto.class);
    }

    public MonthDto getMonthById(Long id){
        Month month = monthRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Month not found"));
        return modelMapper.map(month, MonthDto.class);
    }

    public List<MonthDto> getMonthByUserId(Long userId){
        List<Month> months = monthRepository.findByUserId(userId);
        return months.stream().map(month -> modelMapper.map(month, MonthDto.class)).toList();
    }

    public Long deleteMonthById(Long id){
        monthRepository.deleteById(id);
        return id;
    }


    private Month createMonth(Month existingMonth, MonthDto monthDto, User user){
        if(existingMonth != null){
            existingMonth.setName(monthDto.getName());
            existingMonth.setYear(monthDto.getYear());
            existingMonth.setEarning(monthDto.getEarning());
            return existingMonth;
        } else {
        return Month.builder()
                .name(monthDto.getName())
                .year(monthDto.getYear())
                .earning(monthDto.getEarning())
                .user(user)
                .build();
        }
    }

}
