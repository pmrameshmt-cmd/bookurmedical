
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
    @Autowired
    private FieldEncryptionService encryptionService;

    @Value("${app.frontend-url}")
    private String frontendBaseUrl;

    // ── Registration ──────────────────────────────────────────────────────────

    public void registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        // Use HMAC hash for the uniqueness check (email is encrypted at rest)
        String emailHash = encryptionService.encryptDeterministic(signupRequest.getEmail());
        if (userRepository.existsByEmailHash(emailHash)) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        String verificationToken = java.util.UUID.randomUUID().toString();

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail()); // encrypted on save by listener
        user.setEmailHash(emailHash); // HMAC — queryable
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(signupRequest.getRole());
        user.setFirstName(signupRequest.getFirstName()); // encrypted on save
        user.setLastName(signupRequest.getLastName()); // encrypted on save
        user.setPhoneNumber(signupRequest.getPhoneNumber()); // encrypted on save
        user.setEmailVerificationToken(verificationToken); // encrypted on save
        user.setEmailVerificationTokenHash(
                encryptionService.encryptDeterministic(verificationToken)); // HMAC — queryable
        user.setEmailVerified(false);

        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(
                    signupRequest.getEmail(), signupRequest.getFirstName(), verificationToken);
        } catch (Exception e) {
            String verifyLink = frontendBaseUrl + "/verify-email?token=" + verificationToken;
            System.out.println("------------------------------------------------");
            System.out.println("EMAIL VERIFICATION LINK (Dev Mode):");
            System.out.println(verifyLink);
            System.out.println("------------------------------------------------");
            System.err.println("Failed to send verification email (suppressed for dev): " + e.getMessage());
        }
    }

    // ── Email verification ────────────────────────────────────────────────────

    public void verifyEmail(String token) {
        String tokenHash = encryptionService.encryptDeterministic(token);
        User user = userRepository.findByEmailVerificationTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Error: Invalid or already used verification token!"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null); // clears DB encrypted value
        user.setEmailVerificationTokenHash(null); // clears hash
        userRepository.save(user);

        try {
            // user.getEmail() / getFirstName() are decrypted by the listener after load
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email after verification: " + e.getMessage());
        }
    }

    // ── Forgot password ───────────────────────────────────────────────────────

    public void forgotPassword(String email) {
        String emailHash = encryptionService.encryptDeterministic(email);
        User user = userRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new RuntimeException("Error: User not found with email: " + email));

        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token); // encrypted on save
        user.setResetTokenHash(encryptionService.encryptDeterministic(token)); // HMAC — queryable
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        } catch (Exception e) {
            String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
            System.out.println("------------------------------------------------");
            System.out.println("PASSWORD RESET LINK (Dev Mode):");
            System.out.println(resetLink);
            System.out.println("------------------------------------------------");
            System.err.println("Failed to send password reset email (suppressed for dev): " + e.getMessage());
        }
    }

    // ── Reset password ────────────────────────────────────────────────────────

    public void resetPassword(String token, String newPassword) {
        String tokenHash = encryptionService.encryptDeterministic(token);
        User user = userRepository.findByResetTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Error: Invalid password reset token!"));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Error: Password reset token has expired!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenHash(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    // ── Admin management ──────────────────────────────────────────────────────

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public java.util.List<User> getUsersByRoles(java.util.List<String> roles) {
        return userRepository.findAll().stream()
                .filter(u -> roles.contains(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
