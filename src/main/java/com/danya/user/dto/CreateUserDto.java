package com.danya.user.dto;

import com.danya.user.role.RoleName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CreateUserDto(
        String firstName,
        String lastName,
        @NotNull @NotEmpty Set<RoleName> roleNames
) {
}
