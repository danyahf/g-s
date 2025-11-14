package com.danya.auth;

import com.danya.token.RefreshToken;
import com.danya.token.RefreshTokenService;
import com.danya.user.User;
import com.danya.user.UserRepository;
import com.danya.user.dto.CredentialsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateThrowsBadCredentialsWhenAuthManagerRejects() {
        CredentialsDto payload = new CredentialsDto("nobody", "secret");

        Authentication auth = new UsernamePasswordAuthenticationToken("nobody", "secret");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(payload)
        );

        verify(authenticationManager).authenticate(auth);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticateReturnsTokenWhenAuthManagerAccepts() {
        CredentialsDto payload = new CredentialsDto("alice.john", "pw123");

        User authUser = mock(User.class);
        when(authUser.getId()).thenReturn(42L);

        Authentication authenticationResult = mock(Authentication.class);
        when(authenticationResult.getPrincipal()).thenReturn(authUser);

        Authentication authRequest =
                new UsernamePasswordAuthenticationToken("alice.john", "pw123");

        when(authenticationManager.authenticate(authRequest))
                .thenReturn(authenticationResult);

        when(jwtService.issueAccessToken(authUser))
                .thenReturn("jwt-token-123");

        UUID expectedRefreshUuid = UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(expectedRefreshUuid);

        when(refreshTokenService.issueRefreshToken(42L))
                .thenReturn(refreshToken);

        TokenDto tokenDto = authService.authenticate(payload);

        assertNotNull(tokenDto);
        assertEquals("jwt-token-123", tokenDto.getAccessToken());
        assertEquals(expectedRefreshUuid, tokenDto.getRefreshToken());

        verify(authenticationManager).authenticate(authRequest);
        verify(jwtService).issueAccessToken(authUser);
        verify(refreshTokenService).issueRefreshToken(42L);

        verifyNoMoreInteractions(authenticationManager, jwtService, refreshTokenService);
    }
}
