package com.danya.user;

import com.danya.exception.EntityNotFoundException;
import com.danya.exception.InvalidCurrentPasswordException;
import com.danya.user.dto.PasswordChangeDto;
import com.danya.user.dto.ProfileStatusChangeDto;
import com.danya.user.role.Role;
import com.danya.user.role.RoleName;
import com.danya.user.role.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserGeneratesUsername() {
        String firstName = "Alice";
        String lastName = "Smith";
        String expectedUsername = "Alice.Smith";
        Role role = new Role();
        role.setId(1);
        role.setRoleName(RoleName.ADMIN);

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.ADMIN)).thenReturn(Optional.of(role));

        UserCreationResult result = userService.createUser(firstName, lastName, RoleName.ADMIN);

        assertEquals(expectedUsername, result.user().getUsername());
        assertEquals(firstName, result.user().getFirstName());
        assertEquals(lastName, result.user().getLastName());

        verify(userRepository).existsByUsername(expectedUsername);
    }

    @Test
    void createUserHandlesDuplicateUsername() {
        String firstName = "Bob";
        String lastName = "Jones";
        String baseUsername = "Bob.Jones";
        String uniqueUsername = "Bob.Jones1";
        Role role = new Role();
        role.setId(1);
        role.setRoleName(RoleName.ADMIN);

        when(userRepository.existsByUsername(baseUsername)).thenReturn(true);
        when(userRepository.existsByUsername(uniqueUsername)).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.ADMIN)).thenReturn(Optional.of(role));

        UserCreationResult result = userService.createUser(firstName, lastName, RoleName.ADMIN);

        assertEquals(uniqueUsername, result.user().getUsername());

        verify(userRepository).existsByUsername(baseUsername);
        verify(userRepository).existsByUsername(uniqueUsername);
    }

    @Test
    void createUserHandlesMultipleDuplicateUsernames() {
        String firstName = "Charlie";
        String lastName = "Brown";
        String baseUsername = "Charlie.Brown";
        String candidate1 = "Charlie.Brown1";
        String uniqueUsername = "Charlie.Brown2";
        Role role = new Role();
        role.setId(1);
        role.setRoleName(RoleName.ADMIN);

        when(userRepository.existsByUsername(baseUsername)).thenReturn(true);
        when(userRepository.existsByUsername(candidate1)).thenReturn(true);
        when(userRepository.existsByUsername(uniqueUsername)).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.ADMIN)).thenReturn(Optional.of(role));

        UserCreationResult result = userService.createUser(firstName, lastName, RoleName.ADMIN);

        assertEquals(uniqueUsername, result.user().getUsername());

        verify(userRepository).existsByUsername(baseUsername);
        verify(userRepository).existsByUsername(candidate1);
        verify(userRepository).existsByUsername(uniqueUsername);
    }

    @Test
    void changePasswordThrowsInvalidCurrentPasswordWhenMismatch() {
        User user = new User("First", "Last", "bob", "correctPwd");
        PasswordChangeDto payload =
                new PasswordChangeDto("wrongPwd", "newPwd");

        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));

        assertThrows(
                InvalidCurrentPasswordException.class,
                () -> userService.changePassword(user.getUsername(), payload)
        );

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void changePasswordUpdatesPasswordAndSavesWhenOldMatches() {
        User user = new User("Alice", "Smith", "alice", "oldPwd");
        PasswordChangeDto payload = new PasswordChangeDto("oldPwd", "newPwd");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPwd", "oldPwd")).thenReturn(true);
        when(passwordEncoder.encode("newPwd")).thenReturn("ENC(newPwd)");
        when(userRepository.save(user)).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> userService.changePassword("alice", payload));

        assertEquals("ENC(newPwd)", user.getPassword());

        verify(userRepository).findByUsername("alice");
        verify(passwordEncoder).matches("oldPwd", "oldPwd");
        verify(passwordEncoder).encode("newPwd");
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    void changeStatusThrowsEntityNotFoundWhenUserMissing() {
        String username = "username";
        ProfileStatusChangeDto payload = new ProfileStatusChangeDto(ActivationStatus.ACTIVE);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> userService.changeStatus(username, payload)
        );
        assertEquals("User profile not found", ex.getMessage());

        verify(userRepository, times(1)).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void changeStatusUpdatesActiveFlagAndSavesWhenUserExists() {
        User user = new User("First", "Last", "alice", "pwd");
        user.setActive(false);
        ProfileStatusChangeDto payload = new ProfileStatusChangeDto(ActivationStatus.ACTIVE);

        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> userService.changeStatus(user.getUsername(), payload));

        assertTrue(user.isActive());
        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(userRepository, times(1)).save(user);
        verifyNoMoreInteractions(userRepository);
    }
}
