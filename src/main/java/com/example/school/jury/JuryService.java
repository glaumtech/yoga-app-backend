package com.example.school.jury;


import com.example.school.auth.AuthRep;
import com.example.school.auth.User;
import com.example.school.role.Role;
import com.example.school.role.RoleRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JuryService {
    @Autowired
    private JuryRepository juryRep;

    @Autowired
    private RoleRep roleRep;
    @Autowired
    private AuthRep authRep;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseDto saveUser(RequestDto request) {
        if (juryRep.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new RuntimeException("Judge with name '" + request.getName() + "' already exists!");
        }
        User registerUser = new User();
        registerUser.setUsername(request.getUsername());
        registerUser.setPassword(passwordEncoder.encode(request.getPassword()));
        registerUser.setConfirmPassword(request.getConfirmPassword());
        registerUser.setEmail(request.getEmail());
        if ("judge".equalsIgnoreCase(request.getRole())) {

            Role userRole = roleRep.findByName("JUDGE")
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            registerUser.setRole(userRole);    // store role name
            //  newUser.setRole(userRole);
        }


        //registerUser.setRole(userRole.getName());    // store role name

        authRep.save(registerUser);
        User savedUser = authRep.save(registerUser);
        Jury newUser = new Jury();
        newUser.setName(request.getName());
        newUser.setAddress(request.getAddress());
        newUser.setDesignation(request.getDesignation());
        newUser.setUserId(savedUser.getId());  // store only the id
        Jury savedJury = juryRep.save(newUser);

       // return savedUser;
        return new ResponseDto(
                savedUser.getId(),
                savedJury.getName(),
                registerUser.getUsername(),
                registerUser.getRole().getName(),
                savedJury.getAddress(),
                savedJury.getDesignation(),
                registerUser.getRole().getId()
        );
       // return juryRep.save(newUser);
    }


public ResponseDto updateUser(RequestDto request, Long juryId) {
    // 1️⃣ Check if another jury with the same name exists
    if (juryRep.existsByNameIgnoreCaseAndIdNotAndDeletedFalse(request.getName(), juryId)) {
        throw new RuntimeException("Another jury with this name already exists!");
    }

    // 2️⃣ Fetch the existing Jury
    Jury jury = juryRep.findById(juryId)
            .orElseThrow(() -> new RuntimeException("Jury not found"));

    // 3️⃣ Update Jury details
    jury.setName(request.getName());
    jury.setAddress(request.getAddress());
    jury.setDesignation(request.getDesignation());

    Jury updatedJury = juryRep.save(jury);

    // 4️⃣ Fetch the associated User using stored userId
    User user = authRep.findById(updatedJury.getUserId())
            .orElseThrow(() -> new RuntimeException("Associated User not found"));

    // 5️⃣ Update User details
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());

    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setConfirmPassword(request.getConfirmPassword());
    }

    if (request.getRole() != null) {
        Role role = roleRep.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
    }

    authRep.save(user);

    // 6️⃣ Return combined response
    return new ResponseDto(
            updatedJury.getId(),
            updatedJury.getName(),
            user.getUsername(),
            user.getRole().getName(),
            updatedJury.getAddress(),
            updatedJury.getDesignation(),
            user.getRole().getId()
    );
}

    public List<Jury> getAllJury() {
        return juryRep.findByDeletedFalseOrderByIdDesc(); // only accepted
    }
    public void deleteData(Long id) {
        Optional<Jury> optionalJury = juryRep.findById(id);


        if (optionalJury.isPresent()) {
            Jury jury = optionalJury.get();



            jury.setDeleted(true); // Soft delete
            juryRep.save(jury);
        } else {
            throw new RuntimeException("Judge not found with ID: " + id);
        }
    }

}
