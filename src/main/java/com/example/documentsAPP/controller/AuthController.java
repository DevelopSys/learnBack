package com.example.documentsAPP.controller;

import com.example.documentsAPP.model.User;
import com.example.documentsAPP.security.JwtUtil;
import com.example.documentsAPP.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        user.setRole("USER");

        if (user.getAuthProvider() == null || user.getAuthProvider().isBlank()) {
            user.setAuthProvider(
                    Boolean.TRUE.equals(user.getGoogleLinked()) ? "LOCAL_GOOGLE" : "LOCAL"
            );
        }

        if (user.getGoogleLinked() == null) {
            user.setGoogleLinked(false);
        }

        return ResponseEntity.ok(userService.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Determina si el login es por email o username
            String identifier;
            UserDetails userDetails;

            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                // Login por email → busca el username real para autenticar
                userDetails = userService.loadUserByEmail(request.getEmail());
                identifier = userDetails.getUsername();
            } else {
                identifier = request.getUsername();
                userDetails = userService.loadUserByUsername(identifier);
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            identifier,
                            request.getPassword()
                    )
            );

            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        User user = userService.findByUsernameEntity(authentication.getName());

        return ResponseEntity.ok(new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getEmail(),
                user.getGoogleEmail(),
                user.getGoogleLinked()
        ));
    }

    @PostMapping("/google/link")
    public ResponseEntity<?> linkGoogleAccount(
            @RequestBody GoogleLinkRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        if (request.getGoogleEmail() == null || request.getGoogleEmail().isBlank()) {
            return ResponseEntity.badRequest().body("El email de Google es obligatorio");
        }

        User currentUser = userService.findByUsernameEntity(authentication.getName());

        var existingByGoogleEmail = userService.findByGoogleEmail(request.getGoogleEmail());
        if (existingByGoogleEmail != null &&
                !existingByGoogleEmail.getId().equals(currentUser.getId())) {
            return ResponseEntity.status(409)
                    .body("Esa cuenta de Google ya está vinculada a otro usuario");
        }

        if (request.getGoogleId() != null && !request.getGoogleId().isBlank()) {
            var existingByGoogleId = userService.findByGoogleId(request.getGoogleId());
            if (existingByGoogleId != null &&
                    !existingByGoogleId.getId().equals(currentUser.getId())) {
                return ResponseEntity.status(409)
                        .body("Ese identificador de Google ya está vinculado a otro usuario");
            }
        }

        currentUser.setGoogleId(request.getGoogleId());
        currentUser.setGoogleEmail(request.getGoogleEmail());
        currentUser.setGoogleLinked(true);
        currentUser.setAuthProvider("LOCAL_GOOGLE");

        if (currentUser.getEmail() == null || currentUser.getEmail().isBlank()) {
            currentUser.setEmail(request.getGoogleEmail());
        }

        userService.save(currentUser);

        return ResponseEntity.ok("Cuenta Google vinculada correctamente");
    }

    @PostMapping("/google/unlink")
    public ResponseEntity<?> unlinkGoogleAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        User currentUser = userService.findByUsernameEntity(authentication.getName());

        currentUser.setGoogleId(null);
        currentUser.setGoogleEmail(null);
        currentUser.setGoogleLinked(false);
        currentUser.setAuthProvider("LOCAL");

        userService.save(currentUser);

        return ResponseEntity.ok("Cuenta Google desvinculada correctamente");
    }
}

@Data
class LoginRequest {
    private String username;
    private String password;
    private String email;      // ← añade esto

}

@Data
class LoginResponse {
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }
}

@Data
class MeResponse {
    private Long id;
    private String username;
    private String role;
    private String email;
    private String googleEmail;
    private Boolean googleLinked;

    public MeResponse(Long id, String username, String role,
                      String email, String googleEmail, Boolean googleLinked) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.email = email;
        this.googleEmail = googleEmail;
        this.googleLinked = googleLinked;
    }
}

@Data
class GoogleLinkRequest {
    private String googleId;
    private String googleEmail;
}