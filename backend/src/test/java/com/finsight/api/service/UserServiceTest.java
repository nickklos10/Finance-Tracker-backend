package com.finsight.api.service;

import com.finsight.api.dto.UserDTO;
import com.finsight.api.model.AppUser;
import com.finsight.api.repository.AppUserRepository;
import com.finsight.api.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserServiceImpl userService;

    private AppUser sampleUser;
    private UserDTO sampleUserDTO;

    @BeforeEach
    void setUp() {
        sampleUser = new AppUser();
        sampleUser.setId(1L);
        sampleUser.setAuth0Sub("auth0|123456");
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john@example.com");

        sampleUserDTO = new UserDTO();
        sampleUserDTO.setId(1L);
        sampleUserDTO.setAuth0Sub("auth0|123456");
        sampleUserDTO.setName("John Doe");
        sampleUserDTO.setEmail("john@example.com");
    }

    @Test
    void getCurrentUser_WhenExists_ShouldReturnUser() {
        // Given
        when(currentUserService.getSub()).thenReturn("auth0|123456");
        when(userRepository.findByAuth0Sub("auth0|123456")).thenReturn(Optional.of(sampleUser));

        // When
        UserDTO result = userService.getCurrentUser();

        // Then
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(currentUserService).getSub();
        verify(userRepository).findByAuth0Sub("auth0|123456");
    }

    @Test
    void getCurrentUser_WhenNotExists_ShouldThrowException() {
        // Given
        when(currentUserService.getSub()).thenReturn("nonexistent");
        when(userRepository.findByAuth0Sub("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found: nonexistent");
    }

    @Test
    void updateCurrentUser_WhenExists_ShouldReturnUpdatedUser() {
        // Given
        when(currentUserService.getSub()).thenReturn("auth0|123456");
        when(userRepository.findByAuth0Sub("auth0|123456")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(AppUser.class))).thenReturn(sampleUser);

        // When
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("Jane Doe");
        updateDTO.setEmail("jane@example.com");
        
        UserDTO result = userService.updateCurrentUser(updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(currentUserService).getSub();
        verify(userRepository).findByAuth0Sub("auth0|123456");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void updateCurrentUser_WhenNotExists_ShouldThrowException() {
        // Given
        when(currentUserService.getSub()).thenReturn("nonexistent");
        when(userRepository.findByAuth0Sub("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("Jane Doe");
        
        assertThatThrownBy(() -> userService.updateCurrentUser(updateDTO))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteCurrentUser_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(currentUserService.getSub()).thenReturn("auth0|123456");
        when(userRepository.findByAuth0Sub("auth0|123456")).thenReturn(Optional.of(sampleUser));

        // When
        userService.deleteCurrentUser();

        // Then
        verify(currentUserService).getSub();
        verify(userRepository).findByAuth0Sub("auth0|123456");
        verify(userRepository).delete(sampleUser);
    }

    @Test
    void deleteCurrentUser_WhenNotExists_ShouldThrowException() {
        // Given
        when(currentUserService.getSub()).thenReturn("nonexistent");
        when(userRepository.findByAuth0Sub("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.deleteCurrentUser())
                .isInstanceOf(EntityNotFoundException.class);
    }
} 