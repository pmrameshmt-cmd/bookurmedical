package com.bookurmedical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BookurmedicalApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookurmedicalApplication.class, args);
	}

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner initData(
            com.bookurmedical.repository.UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            createIfMissing(userRepository, passwordEncoder, "admin", "admin@novacura.com", "ADMIN", "System", "Administrator");
            createIfMissing(userRepository, passwordEncoder, "moderate", "moderate@novacura.com", "MODERATE_DOCTOR", "Moderate", "Doctor");
            createIfMissing(userRepository, passwordEncoder, "doctor", "doctor@novacura.com", "DOCTOR", "Specialist", "Doctor");
        };
    }

    private void createIfMissing(
            com.bookurmedical.repository.UserRepository repo,
            org.springframework.security.crypto.password.PasswordEncoder encoder,
            String username, String email, String role, String first, String last) {
        if (!repo.existsByUsername(username)) {
            com.bookurmedical.entity.User user = new com.bookurmedical.entity.User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(encoder.encode("password"));
            user.setRole(role);
            user.setFirstName(first);
            user.setLastName(last);
            user.setEmailVerified(true);
            repo.save(user);
            System.out.println("Initialized System User: " + username + " with role: " + role);
        }
    }
}
