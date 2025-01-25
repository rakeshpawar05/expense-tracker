package com.project.expenseTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "UserName is required")
    @Email(message = "Invalid userName format")
    private String userName;

    @NotBlank(message = "Password is required")
    private String password;
}
