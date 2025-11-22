package app.user;

import app.exception.UserNotFoundException;
import app.notification.service.NotificationService;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.property.UserProperties;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.EditProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    // 1. Mock all dependencies
    // 2. Inject all mocks
    // 3. Think of a scenario to test

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WalletService walletService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private UserProperties userProperties;
    @Mock
    private NotificationService notificationService;

    // Bottom because we are testing user service
    @InjectMocks
    private UserService userService;

    // For updateProfile(UUID id, EditProfileRequest editProfileRequest)
    // 1. If the user does not exist then throw exception
    @Test
    void whenEditUserDetails_andRepositoryReturnsOptionalEmpty_thenThrowsException() {

        // Given
        UUID userId = UUID.randomUUID();
        EditProfileRequest dto = null;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.updateProfile(userId, dto));
    }

    // 2. If there is a user, their data is updated and saved back to the DB
    @Test
    void whenEditUserDetails_andRepositoryReturnsUserFromTheDatabase_thenUpdateTheUserDetailsAndSaveItToTheDatabase() {

        // Given
        UUID userId = UUID.randomUUID();
        EditProfileRequest dto = EditProfileRequest.builder()
                .firstName("Gosho")
                .lastName("Georgiev")
                .profilePicture("www.picture.com")
                .email("joro@gmail.com")
                .build();
        User userRetrievedFromDatabase = User.builder()
                .id(userId)
                .firstName("Vik")
                .lastName("Aleksandrov")
                .profilePicture(null)
                .email("vik@gmail.com")
                .build();
        when(userRepository.findById(any())).thenReturn(Optional.of(userRetrievedFromDatabase));

        // When
        userService.updateProfile(userId, dto);

        // Then
        assertEquals("Gosho", userRetrievedFromDatabase.getFirstName());
        assertEquals("Georgiev", userRetrievedFromDatabase.getLastName());
        assertNotNull(userRetrievedFromDatabase.getProfilePicture());
        assertEquals("www.picture.com", userRetrievedFromDatabase.getProfilePicture());
        assertEquals("joro@gmail.com", userRetrievedFromDatabase.getEmail());
        verify(userRepository).save(userRetrievedFromDatabase);
    }

    // 3. If there is a user and the DTO comes with an email, upsertPreference is called with true
    @Test
    void whenEditUserDetails_andRepositoryReturnsUserAndDtoComesWithNonEmptyEmail_thenInvokeUpsertNotificationPreferenceWithTrue() {

        // Given
        UUID userId = UUID.randomUUID();
        EditProfileRequest dto = EditProfileRequest.builder()
                .email("joro@gmail.com")
                .build();
        User user = User.builder()
                .id(userId)
                .build();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // When
        userService.updateProfile(userId, dto);

        // Then
        verify(notificationService).upsertPreference(userId, true, "joro@gmail.com");
    }

    // 4. If there is a user and DTO comes with an empty email, upsertPreference is called with false
    @Test
    void whenEditUserDetails_andRepositoryReturnsUserAndDtoComesWithEmptyEmail_thenInvokeUpsertNotificationPreferenceWithFalse() {

        // Given
        UUID userId = UUID.randomUUID();
        EditProfileRequest dto = EditProfileRequest.builder()
                .email(null)
                .build();
        User user = User.builder()
                .id(userId)
                .build();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        // When
        userService.updateProfile(userId, dto);

        // Then verify - if it is true that
        verify(notificationService).upsertPreference(userId, false, null);
    }

    // For switchRole(UUID userId)
    // If the user in the DB is Admin, his role becomes User and is saved again in the DB
    @Test
    void whenSwitchRole_andRepositoryReturnsAdmin_thenUserIsUpdatedWithRoleUserAndUpdatedOnNow_andPersistedInTheDatabase() {

        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertEquals(UserRole.USER, user.getRole());
        assertThat(user.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        verify(userRepository).save(user);
    }

    // If the user in the DB is User, his role becomes Admin and is saved again in the DB
    @Test
    void whenSwitchRole_andRepositoryReturnsUser_thenUserIsUpdatedWithRoleAdminAndUpdatedOnNow_andPersistedInTheDatabase() {

        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .role(UserRole.USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId);

        // Then
        assertEquals(UserRole.ADMIN, user.getRole());
        assertThat(user.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        verify(userRepository).save(user);
    }

    // If there is no user - an error is thrown
    @Test
    void whenSwitchRole_andRepositoryReturnsOptionalEmpty_thenThrowsException() {

        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        assertThrows(UserNotFoundException.class, () -> userService.switchRole(userId));
    }
}
