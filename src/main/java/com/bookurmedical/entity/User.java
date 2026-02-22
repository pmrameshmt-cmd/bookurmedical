
package com.bookurmedical.entity;

import com.bookurmedical.annotation.Encrypted;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    private String username; // login key — plain text, queryable

    @Encrypted
    private String email; // PII — stored encrypted

    @Indexed(unique = true, sparse = true)
    private String emailHash; // HMAC of email — used for unique-check & lookup queries

    private String password; // BCrypt hash — already secure

    private String role;

    @Encrypted
    private String firstName; // PII

    @Encrypted
    private String lastName; // PII

    @Encrypted
    private String phoneNumber; // PII

    private boolean isProfileCompleted = false;

    @Encrypted
    private String resetToken; // sensitive token — stored encrypted

    private String resetTokenHash; // HMAC of resetToken — used for lookup

    private java.time.LocalDateTime resetTokenExpiry;

    @Encrypted
    private String emailVerificationToken; // sensitive token — stored encrypted

    private String emailVerificationTokenHash; // HMAC — used for lookup

    private boolean emailVerified = false;
}
