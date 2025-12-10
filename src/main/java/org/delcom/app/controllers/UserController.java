package org.delcom.app.controllers;

import jakarta.validation.Valid;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.ChangePasswordRequest;
import org.delcom.app.entities.User;
import org.delcom.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthContext authContext;

    /**
     * Get Profile User yang sedang login
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        return ResponseEntity.ok(authContext.getAuthUser());
    }

    /**
     * Update Profile
     */
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateProfile(
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        User currentUser = authContext.getAuthUser();
        
        User updatedUser = userService.updateProfile(
                currentUser.getId(),
                name,
                phone,
                address,
                image
        );
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Ganti Password
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = authContext.getAuthUser();

        // Validasi konfirmasi password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Password baru dan konfirmasi tidak cocok");
        }

        try {
            userService.changePassword(
                    currentUser.getId(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok("Password berhasil diubah");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}