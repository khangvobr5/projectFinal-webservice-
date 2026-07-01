package com.badminton.controller;

import com.badminton.dto.UserDTO;
import com.badminton.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetUsers() throws Exception {
        UserDTO user = new UserDTO(1L, "admin", "admin@badminton.com", "ADMIN", true, null);
        Mockito.when(userService.getUsers(0, 10, null)).thenReturn(new PageImpl<>(List.of(user)));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"));
    }

    @Test
    public void testGetUserById() throws Exception {
        UserDTO user = new UserDTO(1L, "admin", "admin@badminton.com", "ADMIN", true, null);
        Mockito.when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        UserDTO updateInfo = new UserDTO(null, "admin_new", "admin@badminton.com", "ADMIN", true, null);
        UserDTO updatedUser = new UserDTO(1L, "admin_new", "admin@badminton.com", "ADMIN", true, null);
        Mockito.when(userService.updateUser(Mockito.eq(1L), Mockito.any(UserDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin_new"));
    }

    @Test
    public void testDeleteUser() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isNoContent());
    }
}
