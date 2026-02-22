
package com.bookurmedical.service;

import com.bookurmedical.dto.SignupRequest;
import com.bookurmedical.entity.User;
import com.bookurmedical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendBaseUrl;

    public void registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(signupRequest.getRole());
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setPhoneNumber(signupRequest.getPhoneNumber());

        // Generate email verification token
        String verificationToken = java.util.UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerified(false);

        userRepository.save(user);

        // Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);
        } catch (Exception e) {
            // Dev fallback: print verification link to console
            String verifyLink = frontendBaseUrl + "/verify-email?token=" + verificationToken;
            System.out.println("------------------------------------------------");
            System.out.println("EMAIL VERIFICATION LINK (Dev Mode):");
            System.out.println(verifyLink);
            System.out.println("------------------------------------------------");
            System.err.println("Failed to send verification email (suppressed for dev): " + e.getMessage());
        }
    }

    public void verifyEmail(String token) {
        com.bookurmedical.entity.User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Error: Invalid or already used verification token!"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null); // clear token after use
        userRepository.save(user);

        // Send welcome email now that address is confirmed
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email after verification: " + e.getMessage());
        }
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: User not found with email: " + email));

        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1)); // Token valid for 1 hour

        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        } catch (Exception e) {
            // Log the token/link for development since email sending might fail without
            // config
            String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
            System.out.println("------------------------------------------------");
            System.out.println("PASSWORD RESET LINK (Dev Mode):");
            System.out.println(resetLink);
            System.out.println("------------------------------------------------");
            System.err.println("Failed to send password reset email (suppressed for dev): " + e.getMessage());
        }
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Error: Invalid password reset token!"));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Error: Password reset token has expired!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }
}
