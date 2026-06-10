package com.athenura.contentflow.user.controller;

import com.athenura.contentflow.user.dto.UserUpdateDTO;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import com.athenura.contentflow.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INTERN')")
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> passwords, Principal principal) {
        userService.changePassword(principal.getName(), passwords.get("oldPassword"), passwords.get("newPassword"));
        return ResponseEntity.ok("Password updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INTERN')")
    @PostMapping("/upload-profile")
    public ResponseEntity<?> uploadProfile(@RequestParam("file") MultipartFile file, Principal principal) {
        String url = userService.uploadProfileImage(principal.getName(), file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INTERN')")
    @PutMapping("/remove-profile")
    public ResponseEntity<?> removeProfilePhoto(Principal principal) {
        userService.removeProfilePhoto(principal.getName());
        return ResponseEntity.ok("Profile photo removed");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INTERN')")
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateDTO updateDTO, Principal principal) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), updateDTO));
    }
}