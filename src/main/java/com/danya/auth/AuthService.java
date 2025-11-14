package com.danya.auth;

import com.danya.exception.EntityNotFoundException;
import com.danya.exception.InvalidTokenException;
import com.danya.token.RefreshToken;
import com.danya.token.RefreshTokenDto;
import com.danya.token.RefreshTokenService;
import com.danya.user.User;
import com.danya.user.UserRepository;
import com.danya.user.dto.CredentialsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public TokenDto authenticate(CredentialsDto payload) {
        String username = payload.username();
        String password = payload.password();

        Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.issueAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.issueRefreshToken(user.getId());
        return new TokenDto(accessToken, refreshToken.getToken());
    }

    @Transactional
    public TokenDto refreshToken(RefreshTokenDto payload) {
        RefreshToken refreshToken = refreshTokenService.findByUuid(payload.getRefreshToken());
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("The refresh token is invalid or expired"));
        refreshTokenService.removeByUuid(refreshToken.getToken());

        String accessToken = jwtService.issueAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.issueRefreshToken(user.getId());

        return new TokenDto(
                accessToken,
                newRefreshToken.getToken()
        );
    }

    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User profile not found"));
        refreshTokenService.removeAll(user.getId());
    }
}
