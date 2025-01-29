package com.project.expenseTracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserResponseDto {

    private Long userId;
    private String name;
    private String token;
}
