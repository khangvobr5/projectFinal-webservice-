package com.badminton.service.impl;

import com.badminton.dto.UserDTO;
import com.badminton.entity.User;
import com.badminton.exception.CustomException;
import com.badminton.repository.UserRepository;
import com.badminton.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;
        
        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        List<UserDTO> dtoList = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return new PageImpl<>(dtoList, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found with id: " + id, HttpStatus.NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found with id: " + id, HttpStatus.NOT_FOUND));

        if (userDTO.getUsername() != null && !userDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                throw new CustomException("Username is already taken", HttpStatus.CONFLICT);
            }
            user.setUsername(userDTO.getUsername());
        }
        
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new CustomException("Email is already registered", HttpStatus.CONFLICT);
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole().toUpperCase());
        }
        
        user.setActive(userDTO.isActive());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new CustomException("User not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
