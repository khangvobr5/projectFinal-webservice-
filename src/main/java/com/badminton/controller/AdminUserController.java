package com.badminton.controller;

import com.badminton.dto.UserDTO;
import com.badminton.dto.response.ResponseDTO;
import com.badminton.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<Page<UserDTO>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<UserDTO> users = userService.getUsers(page, size, search);
        ResponseDTO<Page<UserDTO>> response = ResponseDTO.<Page<UserDTO>>builder()
                .success(true)
                .message("Fetched users successfully")
                .data(users)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder()
                .success(true)
                .message("Fetched user detail successfully")
                .data(user)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder()
                .success(true)
                .message("Updated user successfully")
                .data(updatedUser)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
