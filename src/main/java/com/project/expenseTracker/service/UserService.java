package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.AuthRequest;
import com.project.expenseTracker.dto.UserDto;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.repository.UserRepository;
import com.project.expenseTracker.security.UserInfoDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
        user.setEarning(userDto.getEarning());
        repository.save(user);
        return "User Added Successfully";
    }
}
