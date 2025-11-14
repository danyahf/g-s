package com.danya.user.api;

import com.danya.user.UserService;
import com.danya.user.dto.PasswordChangeDto;
import com.danya.user.dto.ProfileStatusChangeDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController implements UserApi {
    private final UserService userService;

    @PutMapping("/{username}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changePasswordByUsername(
            @PathVariable String username,
            @RequestBody @Valid PasswordChangeDto payload
    ) {
        userService.changePassword(username, payload);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasAnyRole('TRAINEE', 'TRAINER')")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal String username,
            @RequestBody @Valid PasswordChangeDto payload
    ) {
        userService.changePassword(username, payload);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PutMapping("/{username}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatusByUsername(
            @PathVariable String username,
            @RequestBody @Valid ProfileStatusChangeDto payload
    ) {
        userService.changeStatus(username, payload);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PutMapping("/me/status")
    @PreAuthorize("hasAnyRole('TRAINEE', 'TRAINER')")
    public ResponseEntity<Void> changeStatus(
            @AuthenticationPrincipal String username,
            @RequestBody @Valid ProfileStatusChangeDto payload
    ) {
        userService.changeStatus(username, payload);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}
