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
//@AllArgsConstructor
public class UserService implements UserDetailsService {

//    private static final String SECRET_KEY = "kD4hT9lJpQ2uV3xF1Lp0dY1oQe3K2gF5P7vY0T9nK4hH4qH7sW";
//
//    private AuthenticationManager authenticationManager;
//    private PasswordEncoder passwordEncoder;
//    private UserDetailsService userDetailsService;
//    private UserRepository userRepository;

//    public void registerUser(UserDto userDto) {
//        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
//            throw new IllegalArgumentException("Email is already registered!");
//        }
//
//        User user = new User();
//        user.setName(userDto.getName());
//        user.setEmail(userDto.getEmail());
//        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
//        user.setEarning(userDto.getEarning());
//        userRepository.save(user);
//    }

//    public String login(AuthRequest authRequest) {
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
//        );
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUserName());
//        return generateToken(userDetails);
//    }

//    public String authenticate(AuthRequest loginDto) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        return jwtTokenProvider.generateToken(authentication);
//    }

//    private String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }


    @Autowired
    private UserRepository repository;

//    @Autowired
//    private PasswordEncoder encoder;

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
//        return userDetail.map(UserInfoDetails::new)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public String addUser(UserDto userDto) {
        // Encode password before saving the user
//        userDto.setPassword(encoder.encode(userDto.getPassword()));
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setEarning(userDto.getEarning());
        repository.save(user);
        return "User Added Successfully";
    }
}
