package com.badminton.service;

import com.badminton.dto.UserDTO;
import com.badminton.entity.User;
import com.badminton.exception.CustomException;
import com.badminton.repository.UserRepository;
import com.badminton.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUsers_success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .role("CUSTOMER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                anyString(), anyString(), any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.getUsers(0, 10, "test");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    @Test
    void getUsers_noSearch() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .role("CUSTOMER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserDTO> result = userService.getUsers(0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUserById_success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .role("CUSTOMER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> userService.getUserById(999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found with id: 999", exception.getMessage());
    }

    @Test
    void updateUser_success() {
        UserDTO userDTO = UserDTO.builder()
                .username("updateduser")
                .email("updated@test.com")
                .role("ADMIN")
                .active(true)
                .build();

        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .role("CUSTOMER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .username("updateduser")
                .email("updated@test.com")
                .role("ADMIN")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDTO result = userService.updateUser(1L, userDTO);

        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@test.com", result.getEmail());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void updateUser_usernameExists() {
        UserDTO userDTO = UserDTO.builder()
                .username("existinguser")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .role("CUSTOMER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> userService.updateUser(1L, userDTO));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Username is already taken", exception.getMessage());
    }

    @Test
    void updateUser_emailExists() {
        UserDTO userDTO = UserDTO.builder()
                .email("existing@test.com")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .role("CUSTOMER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> userService.updateUser(1L, userDTO));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email is already registered", exception.getMessage());
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> userService.deleteUser(999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found with id: 999", exception.getMessage());
    }
}
