package com.danya.auth;

import com.danya.auth.api.AuthController;
import com.danya.exception.GlobalExceptionHandler;
import com.danya.security.bruteForceProtection.TooManyLoginAttemptsException;
import com.danya.token.RefreshTokenDto;
import com.danya.user.dto.CredentialsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {
    private final AuthService authService = Mockito.mock(AuthService.class);
    private final AuthController authController = new AuthController(authService);
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginReturns200AndTokensOnSuccess() throws Exception {
        TokenDto response = new TokenDto("access-token-123", UUID.randomUUID());
        when(authService.authenticate(any(CredentialsDto.class)))
                .thenReturn(response);

        CredentialsDto body = new CredentialsDto("john.doe", "pass123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken().toString()));

        verify(authService).authenticate(any(CredentialsDto.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    void loginReturns401OnBadCredentials() throws Exception {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authService).authenticate(any(CredentialsDto.class));

        CredentialsDto body = new CredentialsDto("ghost", "wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());

        verify(authService).authenticate(any(CredentialsDto.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    void loginReturns403WhenUserIsLocked() throws Exception {
        doThrow(new TooManyLoginAttemptsException("Test"))
                .when(authService).authenticate(any(CredentialsDto.class));

        CredentialsDto body = new CredentialsDto("test", "test");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        verify(authService).authenticate(any(CredentialsDto.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    void refreshReturns200AndTokensOnSuccess() throws Exception {
        TokenDto response = new TokenDto("access-token-123", UUID.randomUUID());
        when(authService.refreshToken(any(RefreshTokenDto.class)))
                .thenReturn(response);

        UUID refreshToken = UUID.randomUUID();
        RefreshTokenDto body = new RefreshTokenDto(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken().toString()));

        verify(authService).refreshToken(any(RefreshTokenDto.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    void refreshReturns403WhenTokenExpiredOrInvalid() throws Exception {
        doThrow(new TooManyLoginAttemptsException("Test"))
                .when(authService).refreshToken(any(RefreshTokenDto.class));

        UUID refreshToken = UUID.randomUUID();
        RefreshTokenDto body = new RefreshTokenDto(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authService).refreshToken(any(RefreshTokenDto.class));
        verifyNoMoreInteractions(authService);
    }
}
