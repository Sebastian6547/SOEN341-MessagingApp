package com.messagingApp.messagingApp_backend.controllers;

import com.messagingApp.messagingApp_backend.services.AdminService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = AdminController.class)
@ActiveProfiles("test")
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Test
    void isAdmin_ReturnsTrueForAdminUser() throws Exception {
        // Arrange
        String username = "adminUser";
        Mockito.when(adminService.isAdmin(username)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/admin/checkAdmin").accept(MediaType.APPLICATION_JSON).param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isAdmin_ReturnsFalseForNonAdminUser() throws Exception {
        // Arrange
        String username = "regularUser";
        Mockito.when(adminService.isAdmin(username)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/admin/checkAdmin").accept(MediaType.APPLICATION_JSON).param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void updateUserRole_SuccessfullyUpdatesRole() throws Exception {
        // Arrange
        String currentUsername = "adminUser";
        String targetUsername = "testUser";
        String newRole = "ADMIN";

        Mockito.when(adminService.updateUserRole(currentUsername, targetUsername, newRole)).thenReturn(1);

        // Act & Assert
        mockMvc.perform(put("/api/admin/updateRole")
                        .param("currentUsername", currentUsername)
                        .param("targetUsername", targetUsername)
                        .param("newRole", newRole))
                .andExpect(status().isOk())
                .andExpect(content().string("User role updated successfully."));
    }

    @Test
    void updateUserRole_FailedToUpdateRole_ReturnsForbidden() throws Exception {
        // Arrange
        String currentUsername = "adminUser";
        String targetUsername = "testUser";
        String newRole = "ADMIN";

        Mockito.when(adminService.updateUserRole(currentUsername, targetUsername, newRole)).thenReturn(0);

        // Act & Assert
        mockMvc.perform(put("/api/admin/updateRole")
                        .param("currentUsername", currentUsername)
                        .param("targetUsername", targetUsername)
                        .param("newRole", newRole))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Permission denied or failed to update user role."));
    }

    @Test
    void deleteMessage_SuccessfullyDeletesMessage() throws Exception {
        // Arrange
        Long messageId = 1L;

        Mockito.when(adminService.deleteMessage(messageId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/deleteMessage/{messageId}", messageId))
                .andExpect(status().isOk())
                .andExpect(content().string("Message deleted successfully."));
    }

    @Test
    void deleteMessage_FailedToDeleteMessage_ReturnsForbidden() throws Exception {
        // Arrange
        Long messageId = 1L;

        Mockito.when(adminService.deleteMessage(messageId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/deleteMessage/{messageId}", messageId))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Something went wrong"));
    }


}
