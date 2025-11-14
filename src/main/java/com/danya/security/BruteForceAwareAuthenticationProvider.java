package com.danya.security;

import com.danya.security.bruteForceProtection.BruteForceProtector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class BruteForceAwareAuthenticationProvider extends DaoAuthenticationProvider {
    private final BruteForceProtector bruteForceProtector;

    public BruteForceAwareAuthenticationProvider(UserDetailsService uds,
                                                 PasswordEncoder passwordEncoder,
                                                 BruteForceProtector bruteForceProtector) {
        this.bruteForceProtector = bruteForceProtector;
        super.setUserDetailsService(uds);
        super.setPasswordEncoder(passwordEncoder);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();

        bruteForceProtector.assertLoginAllowed(username);
        try {
            Authentication result = super.authenticate(authentication);
            bruteForceProtector.recordLoginSuccess(username);
            return result;
        } catch (AuthenticationException ex) {
            bruteForceProtector.recordLoginFailure(username);
            throw ex;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
