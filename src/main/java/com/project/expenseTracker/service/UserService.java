package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.AuthRequest;
import com.project.expenseTracker.dto.UserDomainDto;
import com.project.expenseTracker.dto.UserDto;
import com.project.expenseTracker.dto.UserResponseDto;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.DuplicateResourceException;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.UserRepository;
import com.project.expenseTracker.security.UserInfoDetails;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userDetail = repository.findByEmail(username); // Assuming 'email' is used as username

        // Converting UserInfo to UserDetails
        if(userDetail.isEmpty()){
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserInfoDetails(modelMapper.map(userDetail, UserDto.class));
    }

    public String addUser(UserDto userDto) {
        // Encode password before saving the user
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
//        user.setEarning(userDto.getEarning());
        Optional<User> optionalUser = repository.findByEmail(user.getEmail());
        if(optionalUser.isPresent()){
            throw new DuplicateResourceException("User already registered");
        }
//                .(
//                () -> new DuplicateResourceException("User already register")
//        );
        repository.save(user);
        return "User Added Successfully";
    }

    public UserResponseDto getUserResponse(AuthRequest authRequest, String token){
        Optional<User> userDetail = repository.findByEmail(authRequest.getUserName()); // Assuming 'email' is used as username

        // Converting UserInfo to UserDetails
        if(userDetail.isEmpty()){
            throw new UsernameNotFoundException("User not found: " + authRequest.getUserName());
        }

        return UserResponseDto.builder()
                .name(userDetail.get().getName())
                .userId(userDetail.get().getId())
                .token(token)
                .build();
    }

    public UserDomainDto getUserInfo(Long id){
        User user = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapEntityToDto(user);
    }

    public static UserDomainDto mapEntityToDto(User user){
        return UserDomainDto.builder()
                .id(user.getId())
                .name(user.getName())
                .months(user.getMonths().stream().map(MonthService::mapEntityToDto).toList())
                .categories(user.getCategories().stream().map(CategoryService::mapEntityToDto).toList())
                .expenses(user.getExpenses().stream().map(ExpenseService::mapEntityToDTo).toList())
                .build();
    }
}
