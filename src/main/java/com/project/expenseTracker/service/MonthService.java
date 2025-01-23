package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.MonthDto;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.repository.MonthRepository;
import com.project.expenseTracker.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.Optional;

@Service
@AllArgsConstructor
public class MonthService {

    private MonthRepository monthRepository;
    private UserRepository userRepository;
    private ModelMapper modelMapper;

    public long createMonth(MonthDto monthDto){
        Month mapped = modelMapper.map(monthDto, Month.class);
//        Optional<User> user = userRepository.findById(mapped.getId());
//        mapped.setUser(user.get());
//        monthRepository.getReferenceById(1);
        Month month = monthRepository.save(mapped);
        return  month.getId();
    }
}
