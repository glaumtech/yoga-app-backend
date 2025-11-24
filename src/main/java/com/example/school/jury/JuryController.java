package com.example.school.jury;


import com.example.school.auth.AuthRep;
import com.example.school.auth.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/judge")
public class JuryController {

    @Autowired
    private JuryService juryService;
    @Autowired
    private AuthRep authRep;
    @PostMapping("/register")
    public ResponseEntity<?> submit(@RequestBody RequestDto request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Call service to save user
            ResponseDto savedUser = juryService.saveUser(request);

            // Build response map
//            Map<String, Object> user = new HashMap<>();

//            user.put("username", savedUser.getUsername());
//            user.put("address", savedUser.getAddress());
//            user.put("designation",savedUser.getDesignation());
//            user.put("roleId", savedUser.getRole() != null ? savedUser.getRole().getId() : null);
//            user.put("roleName", savedUser.getRole() != null ? savedUser.getRole().getName() : null);
//            user.put("id", savedUser.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("user", savedUser);

            response.put("status", "success");
            response.put("message", "Data saved successfully!");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody RequestDto request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Call service to update user
            ResponseDto updatedUser = juryService.updateUser(request, id);

            // Build response map
//            Map<String, Object> user = new HashMap<>();
//            user.put("id", updatedUser.getId());
//            user.put("userName", updatedUser.getUsername());
//            user.put("address", updatedUser.getAddress());
//            user.put("designation", updatedUser.getDesignation());
//            user.put("roleId", updatedUser.getRole() != null ? updatedUser.getRole().getId() : null);
//            user.put("roleName", updatedUser.getRole() != null ? updatedUser.getRole().getName() : null);

            Map<String, Object> data = new HashMap<>();
            data.put("user", updatedUser);

            response.put("status", "success");
            response.put("message", "Judge updated successfully!");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllJury() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Jury> jury = juryService.getAllJury();

            List<Map<String, Object>> users = new ArrayList<>();
            for (Jury e : jury) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", e.getId());
                userMap.put("name", e.getName());
//                userMap.put("userName", e.getUsername());
                userMap.put("address", e.getAddress());
                userMap.put("designation", e.getDesignation());
//                userMap.put("roleId", e.getRole() != null ? e.getRole().getId() : null);
//                userMap.put("roleName", e.getRole() != null ? e.getRole().getName() : null);
                // Fetch from Register
                Optional<User> regOpt = authRep.findByJuryId(e.getId());
                if (regOpt.isPresent()) {
                    User reg = regOpt.get();
                    userMap.put("userName", reg.getUsername());

                    userMap.put("roleId", reg.getRole() != null ? reg.getRole().getId() : null);
                    userMap.put("roleName", reg.getRole() != null ? reg.getRole().getName() : null);

                } else {
                    userMap.put("userName", null);
                    userMap.put("roleName", null);
                }

                users.add(userMap);
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("users", users);

            response.put("status", "success");
            response.put("message", "Judges retrieved successfully!");
            response.put("data", dataMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error retrieving judge!");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteJury(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {


            juryService.deleteData(id);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("users", "");

            response.put("status", "success");
            response.put("message", "Judge deleted successfully!");
            response.put("data", dataMap);


            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error in deleting data!");
            return ResponseEntity.badRequest().body(response);
        }
    }

}
