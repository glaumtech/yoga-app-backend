package com.example.school.auth;

import com.example.school.jury.Jury;
import com.example.school.jury.JuryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private JuryRepository juryRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> submit(@RequestBody UserRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Check if email exists
            if (authService.findByEmailOrPhoneNo(request.getEmail(),request.getPhoneNo()).isPresent()) {
                response.put("status", "error");
                response.put("message", "UserName already exists!");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // Save user through service
            User savedUser = authService.saveUser(request);

            // Build response data
            Map<String, Object> user = new HashMap<>();
            user.put("email", savedUser.getEmail());
            user.put("username", savedUser.getUsername());
            user.put("phone_no", savedUser.getPhoneNo());
            user.put("role_id", savedUser.getRole() != null ? savedUser.getRole().getId() : null);
            user.put("role_name", savedUser.getRole() != null ? savedUser.getRole().getName() : null);
            user.put("id", savedUser.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("user", user);

            // Final response
            response.put("status", "success");
            response.put("message", "Data saved successfully!");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest request) {
        Optional<User> user = authService.findByEmail(request.getEmail());
        System.out.println("Raw password: '" + request.getPassword() + "'"+"user"+user.get());
        System.out.println("Hashed password: '" + user.get().getPassword() + "'");
        System.out.println("Password match: " + passwordEncoder.matches(request.getPassword(), user.get().getPassword()));
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        if (user.isPresent() && passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {

            UserDetails userDetail = userDetailsService.loadUserByUsername(request.getEmail());
            User optionaUser = user.get();
            String roleName = optionaUser.getRole() != null ? optionaUser.getRole().getName() : null;

            String token = jwtTokenUtil.generateToken(userDetail, user.get().getId());


            String adminOrRoleName = null;
// Determine what to send in response based on role
            if (user.get().getRole() != null) {
                if (user.get().getRole().getId() == 1) {
                    adminOrRoleName = user.get().getRole().getName(); // role name if role_id = 3
                } else  {
                    adminOrRoleName = user.get().getRole().getName(); // branch name for other roles
                }
            }


            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("username", user.get().getUsername());
            userDetails.put("email", user.get().getEmail());
            userDetails.put("phone_no", user.get().getPhoneNo());
            userDetails.put("role_name", adminOrRoleName);
            userDetails.put("id",user.get().getId());
            userDetails.put("role_id", user.get().getRole() != null ? user.get().getRole().getId() : null);
// Check if frontend role matches DB role


            data.put("token", token);
            data.put("token_type", "Bearer");
            data.put("user", userDetails); // nested user details


            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("data", data);


            return ResponseEntity.ok(response);
          //  return ResponseEntity.ok(response);
        } else {
            response.put("status", "failure");
            response.put("message", "Invalid credentials");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid credentials"));
        }
    }

@PostMapping("/logout")
public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization") String token) {
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> data = new HashMap<>();

    if (token != null && token.startsWith("Bearer ")) {
        String jwtToken = token.substring(7);
        // TODO: Add token to blacklist if implementing server-side invalidation
        data.put("tokenInvalidated", true);
        response.put("status", "success");
        response.put("message", "Logged out successfully");
        response.put("data", data);
        return ResponseEntity.ok(response);
    } else {
        data.put("tokenInvalidated", false);
        response.put("status", "fail");
        response.put("message", "No valid token provided");
        response.put("data", data);
        return ResponseEntity.status(400).body(response); // 400 Bad Request
    }
}


    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getJuryIdByUserId(@PathVariable Long userId) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        try {

            if (userId == null) {
                response.put("status", false);
                response.put("message", "User ID cannot be null");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Jury> juryOptional = juryRepository.findByUserId(userId);

            if (juryOptional.isEmpty()) {
                response.put("status", "failure");
                response.put("message", "Jury not found for this user");
                response.put("data", null);
                return ResponseEntity.status(404).body(response);
            }

            // Jury found → return only juryId
            data.put("juryId", juryOptional.get().getId());

            response.put("status", "success");
            response.put("message", "Jury found");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "failure");
            response.put("message", "Something went wrong: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }



}
