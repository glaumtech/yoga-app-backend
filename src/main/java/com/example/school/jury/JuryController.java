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
    @Autowired
    private JuryRepository juryRepository;
    @PostMapping("/register")
    public ResponseEntity<?> submit(@RequestBody RequestDto request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Call service to save user
            ResponseDto savedUser = juryService.saveUser(request);



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

                List<Jury> regList = new ArrayList<>();
                if (e.getUserId() != null) {
                    regList = juryRepository.findAllByUserId(e.getUserId());
                }

                regList.stream().findFirst().ifPresent(juryItem -> {
                        authRep.findById(juryItem.getUserId()).ifPresent(user -> {
                            userMap.put("userName", user.getUsername());
                            userMap.put("email",user.getEmail());
                            userMap.put("roleId", user.getRole() != null ? user.getRole().getId() : null);
                            userMap.put("roleName", user.getRole() != null ? user.getRole().getName() : null);
                        });
                    });




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
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/judge-id/{userId}")
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

            if (!juryOptional.isPresent()) {
                response.put("status", "failure");
                response.put("message", "Jury not found for this user");
                response.put("data", null);
                return ResponseEntity.status(404).body(response);
            }
            Map<String, Object> jury = new HashMap<>();
            // Jury found → return only juryId
            jury.put("id", juryOptional.get().getId());
            jury.put("name",juryOptional.get().getName());
            jury.put("designation",juryOptional.get().getDesignation());
            jury.put("address",juryOptional.get().getAddress());
            data.put("jury",jury);
            response.put("status", "success");
            response.put("message", "Jury found");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "failure");
            response.put("message",e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

}
