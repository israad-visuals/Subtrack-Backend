package com.subtrack.controller;

import com.subtrack.dto.UpdateProfileRequest;
import com.subtrack.model.User;
import com.subtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.subtrack.repository.PasswordResetTokenRepository;
import com.subtrack.repository.SubscriptionRepository;
import jakarta.transaction.Transactional;
import java.util.Map;
import com.subtrack.dto.ChangePasswordRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                java.util.Map.of(
                        "userId", user.getId(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail(),
                        "createdAt", user.getCreatedAt()
                                .toString()
                ));
    }
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getFirstName() != null
                && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null
                && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        if (request.getPassword() != null
                && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(
                    request.getPassword()));
        }

        userRepository.save(user);

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message", "Profile updated",
                        "userId", user.getId(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail()
                ));
    }
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> deleteAccount(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    // Delete all user's subscriptions
                    subscriptionRepository.deleteAllByUserId(id);

                    // Delete any password reset tokens
                    passwordResetTokenRepository.deleteByEmail(user.getEmail());

                    // Delete the user
                    userRepository.delete(user);

                    return ResponseEntity.ok(Map.of(
                            "message", "Account and all associated data have been permanently deleted"
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {

        return userRepository.findById(userId)
                .map(user -> {
                    // Check if current password is correct
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "Current password is incorrect"
                        ));
                    }

                    // Check new password is different from current
                    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "New password must be different from current password"
                        ));
                    }

                    // Update the password
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    userRepository.save(user);

                    return ResponseEntity.ok(Map.of(
                            "message", "Password changed successfully"
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}