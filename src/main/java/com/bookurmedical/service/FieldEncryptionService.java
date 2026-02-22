package com.bookurmedical.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Dual-mode field-level encryption service.
 *
 * ┌──────────────────────┬────────────────────────────────────────────────────┐
 * │ Mode │ When to use │
 * ├──────────────────────┼────────────────────────────────────────────────────┤
 * │ encrypt() │ At-rest storage (@Encrypted fields on save). │
 * │ → AES-256-GCM │ Random IV → different ciphertext each time. │
 * │ → prefix ENC:: │ Authenticated (tamper-proof). │
 * ├──────────────────────┼────────────────────────────────────────────────────┤
 * │ encryptDeterministic │ Before querying encrypted fields (findByEmail…). │
 * │ → HMAC-SHA256 │ Same input always → same output → queryable. │
 * │ → prefix ENCD:: │ No random component — do NOT use for storage. │
 * └──────────────────────┴────────────────────────────────────────────────────┘
 *
 * Data stored in MongoDB always uses the random (ENC::) variant.
 * Query parameters use the deterministic (ENCD::) variant to match the stored
 * index.
 *
 * ⚠ Important: existing documents in MongoDB that were stored WITHOUT
 * encryption
 * will NOT be decryptable automatically. You should run a one-time migration
 * script (or clear the collection in dev) after enabling this feature.
 */
@Service
public class FieldEncryptionService {

    private static final String RAND_PREFIX = "ENC::";
    private static final String DET_PREFIX = "ENCD::";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey aesKey;
    private final SecretKey hmacKey;

    public FieldEncryptionService(
            @Value("${app.encryption.secret:${APP_ENCRYPTION_PASSWORD:password@1app}}") String rawSecret) {
        byte[] hash = sha256(rawSecret.getBytes(StandardCharsets.UTF_8));
        this.aesKey = new SecretKeySpec(Arrays.copyOf(hash, 32), "AES");
        this.hmacKey = new SecretKeySpec(sha256(("HMAC:" + rawSecret).getBytes(StandardCharsets.UTF_8)), "HmacSHA256");
    }

    // ── Random AES-GCM (for storage) ─────────────────────────────────────────

    /** Encrypts with a random IV. Use this for at-rest storage. Idempotent. */
    public String encrypt(String plain) {
        if (plain == null || plain.isBlank() || isEncrypted(plain))
            return plain;
        try {
            byte[] iv = newIv();
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buf = ByteBuffer.allocate(iv.length + ct.length);
            buf.put(iv);
            buf.put(ct);
            return RAND_PREFIX + Base64.getUrlEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /** Decrypts an ENC:: value. Idempotent — plain text passes through. */
    public String decrypt(String cipher) {
        if (cipher == null || cipher.isBlank() || !cipher.startsWith(RAND_PREFIX))
            return cipher;
        try {
            byte[] data = Base64.getUrlDecoder().decode(cipher.substring(RAND_PREFIX.length()));
            ByteBuffer buf = ByteBuffer.wrap(data);
            byte[] iv = new byte[IV_LEN];
            buf.get(iv);
            byte[] ct = new byte[buf.remaining()];
            buf.get(ct);
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(TAG_BITS, iv));
            return new String(c.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    // ── Deterministic HMAC (for querying) ────────────────────────────────────

    /**
     * Produces a deterministic token for a given plain-text value.
     * Use this when you need to search for a record by an encrypted field.
     * The result starts with ENCD:: and is NOT reversible.
     */
    public String encryptDeterministic(String plain) {
        if (plain == null || plain.isBlank())
            return plain;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            byte[] tag = mac.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return DET_PREFIX + Base64.getUrlEncoder().encodeToString(tag);
        } catch (Exception e) {
            throw new RuntimeException("Deterministic encryption failed", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean isEncrypted(String v) {
        return v != null && (v.startsWith(RAND_PREFIX) || v.startsWith(DET_PREFIX));
    }

    private static byte[] newIv() {
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
