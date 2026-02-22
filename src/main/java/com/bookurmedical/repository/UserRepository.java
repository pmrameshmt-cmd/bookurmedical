
package com.bookurmedical.repository;

import com.bookurmedical.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    // ── Hash-based lookups (encrypted fields) ─────────────────────────────────
    // Query by HMAC hash, not the raw encrypted value

    /**
     * Check if an email is already registered (pass
     * encryptionService.encryptDeterministic(email))
     */
    Boolean existsByEmailHash(String emailHash);

    /** Find user by email (pass encryptionService.encryptDeterministic(email)) */
    Optional<User> findByEmailHash(String emailHash);

    /**
     * Find user by password-reset token (pass
     * encryptionService.encryptDeterministic(token))
     */
    Optional<User> findByResetTokenHash(String resetTokenHash);

    /**
     * Find user by email-verification token (pass
     * encryptionService.encryptDeterministic(token))
     */
    Optional<User> findByEmailVerificationTokenHash(String emailVerificationTokenHash);
}
