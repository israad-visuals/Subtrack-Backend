package com.subtrack.service;

import com.subtrack.model.PasswordResetToken;
import com.subtrack.model.User;
import com.subtrack.repository.PasswordResetTokenRepository;
import com.subtrack.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int EXPIRY_MINUTES = 15;
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email"));

        // Delete any old tokens for this email
        tokenRepository.deleteByEmail(email);

        // Create a new random token
        String token = UUID.randomUUID().toString();

        // Set expiry to 15 minutes from now
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        // Save the token in the database
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setExpiryDate(expiry);
        tokenRepository.save(resetToken);

        // Send the email with the reset link
        String resetLink = "https://systemtest.spiraml.com/reset-password?token=" + token;
        String subject = "SubTrack - Password Reset Request";
        String body = "Hi " + user.getFirstName() + ",\n\n"
                + "You requested a password reset. Click the link below to set a new password:\n\n"
                + resetLink + "\n\n"
                + "This link will expire in " + EXPIRY_MINUTES + " minutes.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "– SubTrack Team";

        emailService.sendEmail(user.getEmail(), subject, body);
    }
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if the token has expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        // Find the user and update their password
        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the used token
        tokenRepository.delete(resetToken);
    }
}