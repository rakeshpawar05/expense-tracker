package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.AuthRequest;
import com.project.expenseTracker.dto.UserDto;
import com.project.expenseTracker.security.JwtService;
import com.project.expenseTracker.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class UserController {

//    @Autowired
    private UserService userService;

//    @Autowired
    private JwtService jwtService;

//    @Autowired
    private AuthenticationManager authenticationManager;

    //    @Autowired
    private PasswordEncoder encoder;

//    @PostMapping("/register")
//    public ResponseEntity<String> register(@RequestBody UserDto userDto) {
//        userService.registerUser(userDto);
//        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestBody AuthRequest loginDto) {
//        String token = userService.login(loginDto);
//        return ResponseEntity.ok(token);
//    }


    @PostMapping("/register")
    public String addNewUser(@RequestBody UserDto userDto) {
        userDto.setPassword(encoder.encode(userDto.getPassword()));
        return userService.addUser(userDto);
    }

    @PostMapping("/login")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(authRequest.getUserName());
        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }
}
