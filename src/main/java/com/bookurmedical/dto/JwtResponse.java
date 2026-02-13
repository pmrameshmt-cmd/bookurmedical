
package com.bookurmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean isProfileCompleted;
}
