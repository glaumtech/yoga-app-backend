package com.example.school.auth;

import com.example.school.role.Role;
import com.example.school.role.RoleRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private RoleRep roleRep;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @PostMapping("/register")
    public ResponseEntity submit(@RequestBody  UserRequest request){
        Map<String, Object> response = new HashMap<>();
        if (authService.findByName(request.getUsername()).isPresent()) {
            response.put("status", "error");
            response.put("message", "UserName already exists!");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
           // return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Name already exists!"));
        }

        Register newUser=new Register();
        newUser.setPhoneNo(request.getPhoneNo());
        if ("user".equalsIgnoreCase(request.getRole())) {
            Role userRole = roleRep.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            newUser.setRole(userRole); // set the role object to the user

        }

        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        newUser.setAccepted(false);

//        Role role=roleRep.findById()
//        newUser.setRole();

        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // ✅ Hash password before saving
        newUser.setConfirmPassword(request.getPassword());
        authService.saves(newUser);
       // return ResponseEntity.ok(Collections.singletonMap("message", "Data saved successfully!"));
        Map<String, Object> data = new HashMap<>();
        data.put("name", newUser.getUsername());
        data.put("email", newUser.getEmail());
        data.put("username", newUser.getUsername());
        data.put("phone_no",newUser.getPhoneNo());

        // Build response
        response.put("status", "success");
        response.put("message", "Data saved successfully!");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest request) {
        Optional<Register> user = authService.findByName(request.getUsername());
        System.out.println("Raw password: '" + request.getPassword() + "'"+"user"+user.get());
        System.out.println("Hashed password: '" + user.get().getPassword() + "'");
        System.out.println("Password match: " + passwordEncoder.matches(request.getPassword(), user.get().getPassword()));

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        if (user.isPresent() && passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {

            UserDetails userDetail = userDetailsService.loadUserByUsername(request.getUsername());



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

//            Map<String,Object> response = new HashMap<>();
//            response.put("message", "Login successful");
//            response.put("username", user.get().getName());
//
//            response.put("token", token);
//            response.put("token_type", "Bearer");
//            response.put("name", adminOrRoleName);
//            response.put("role_id", user.get().getRole() != null ? user.get().getRole().getId() : null); // ✅ correc
//            data.put("username", user.get().getUsername());
//            data.put("email",user.get().getEmail());
//            data.put("phone_no",user.get().getPhoneNo());
//            data.put("token", token);
//            data.put("token_type", "Bearer");
//            data.put("role_name", adminOrRoleName);
//            data.put("role_id", user.get().getRole() != null ? user.get().getRole().getId() : null);
//
//            // ✅ top-level structure
//            response.put("status", "success");
//            response.put("message", "Login successful");
//            response.put("data", data);

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("username", user.get().getUsername());
            userDetails.put("email", user.get().getEmail());
            userDetails.put("phone_no", user.get().getPhoneNo());
            userDetails.put("role_name", adminOrRoleName);
            userDetails.put("id",user.get().getId());
            userDetails.put("role_id", user.get().getRole() != null ? user.get().getRole().getId() : null);


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
    public ResponseEntity<?> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
        //return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
    }
}
