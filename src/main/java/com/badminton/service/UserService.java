package com.badminton.service;

import com.badminton.dto.UserDTO;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserDTO> getUsers(int page, int size, String search);
    UserDTO getUserById(Long id);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
}
