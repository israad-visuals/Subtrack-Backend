package com.subtrack.controller;

import com.subtrack.dto.ForgotPasswordRequest;
import com.subtrack.dto.ResetPasswordRequest;
import com.subtrack.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "If an account exists with that email, a reset link has been sent."));
        } catch (Exception e) {
            System.out.println("FORGOT PASSWORD ERROR: " + e.getMessage());
            return ResponseEntity.ok(Map.of("message", "If an account exists with that email, a reset link has been sent."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. You can now log in with your new password."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}