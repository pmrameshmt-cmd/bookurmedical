package com.bookurmedical.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark any String field on a MongoDB @Document entity with this annotation
 * to have it transparently AES-256 encrypted before saving to the database
 * and decrypted when loading back into the application.
 *
 * Usage:
 * 
 * @Encrypted
 *            private String email;
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypted {
}
