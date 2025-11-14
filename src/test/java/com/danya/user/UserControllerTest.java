package com.danya.user;

import com.danya.SecurityTestConfig;
import com.danya.auth.JwtService;
import com.danya.auth.filter.ErrorResponseWriter;
import com.danya.exception.EntityNotFoundException;
import com.danya.exception.InvalidCurrentPasswordException;
import com.danya.user.api.UserController;
import com.danya.user.dto.PasswordChangeDto;
import com.danya.user.dto.ProfileStatusChangeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityTestConfig.class)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ErrorResponseWriter errorResponseWriter;

    @Test
    void changePasswordReturns204ForValidPayload() throws Exception {
        PasswordChangeDto body = new PasswordChangeDto("oldPass123", "newPass456");

        mockMvc.perform(put("/users/{username}/password", "john.doe")
                        .with(authentication(getAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq("john.doe"), any(PasswordChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void meChangePasswordReturns204ForValidPayload() throws Exception {
        PasswordChangeDto body = new PasswordChangeDto("oldPass123", "newPass456");

        mockMvc.perform(put("/users/me/password")
                        .with(authentication(getTraineeUser("john.doe")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq("john.doe"), any(PasswordChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void changePasswordReturns404WhenEntityNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User profile not found"))
                .when(userService).changePassword(eq("ghost"), any(PasswordChangeDto.class));

        PasswordChangeDto body = new PasswordChangeDto("old", "newStrong");

        mockMvc.perform(put("/users/{username}/password", "ghost")
                        .with(authentication(getAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        verify(userService).changePassword(eq("ghost"), any(PasswordChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void meChangePasswordReturns404WhenEntityNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User profile not found"))
                .when(userService).changePassword(eq("ghost.ghost"), any(PasswordChangeDto.class));

        PasswordChangeDto body = new PasswordChangeDto("old", "newStrong");

        mockMvc.perform(put("/users/me/password")
                        .with(authentication(getTraineeUser("ghost.ghost")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        verify(userService).changePassword(eq("ghost.ghost"), any(PasswordChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void changePasswordReturns400WhenCurrentPasswordInvalid() throws Exception {
        doThrow(new InvalidCurrentPasswordException())
                .when(userService).changePassword(eq("john.doe"), any(PasswordChangeDto.class));

        PasswordChangeDto body = new PasswordChangeDto("wrongOld", "newStrong");

        mockMvc.perform(put("/users/{username}/password", "john.doe")
                        .with(authentication(getAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(userService).changePassword(eq("john.doe"), any(PasswordChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void meChangePasswordReturns400WhenCurrentPasswordInvalid() throws Exception {
        doThrow(new InvalidCurrentPasswordException())
                .when(userService).changePassword(eq("john.doe"), any(PasswordChangeDto.class));

        PasswordChangeDto body = new PasswordChangeDto("wrongOld", "newStrong");

        mockMvc.perform(put("/users/me/password")
                        .with(authentication(getTraineeUser("john.doe")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(userService).changePassword(eq("john.doe"), any(PasswordChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void changeStatusReturns204ForValidPayload() throws Exception {
        ProfileStatusChangeDto body = new ProfileStatusChangeDto(ActivationStatus.ACTIVE);

        mockMvc.perform(put("/users/{username}/status", "john.doe")
                        .with(authentication(getAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());

        verify(userService).changeStatus(eq("john.doe"), any(ProfileStatusChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void meChangeStatusReturns204ForValidPayload() throws Exception {
        ProfileStatusChangeDto body = new ProfileStatusChangeDto(ActivationStatus.ACTIVE);

        mockMvc.perform(put("/users/me/status")
                        .with(authentication(getTraineeUser("john.doe")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());

        verify(userService).changeStatus(eq("john.doe"), any(ProfileStatusChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void changeStatusReturns404WhenEntityNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User profile not found"))
                .when(userService).changeStatus(eq("ghost.ghost"), any(ProfileStatusChangeDto.class));

        ProfileStatusChangeDto body = new ProfileStatusChangeDto(ActivationStatus.ACTIVE);

        mockMvc.perform(put("/users/{username}/status", "ghost.ghost")
                        .with(authentication(getAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).changeStatus(eq("ghost.ghost"), any(ProfileStatusChangeDto.class));
        verifyNoMoreInteractions(userService);
    }

    private static Authentication getTraineeUser(String username) {
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TRAINEE"))
        );
    }

    private static Authentication getAdmin() {
        return new UsernamePasswordAuthenticationToken(
                "admin.admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
