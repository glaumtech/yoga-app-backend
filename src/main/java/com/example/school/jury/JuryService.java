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

        Jury newUser = new Jury();

        newUser.setName(request.getName());
       // newUser.setUsername(request.getUsername());

        // Set role
//        if ("judge".equalsIgnoreCase(request.getRole())) {
//
//            Role userRole = roleRep.findByName("JUDGE")
//                    .orElseThrow(() -> new RuntimeException("Role not found"));
//
//            newUser.setRole(userRole);
//        }

        newUser.setAddress(request.getAddress());
        newUser.setDesignation(request.getDesignation());

        Jury savedUser = juryRep.save(newUser);
//        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
//        newUser.setConfirmPassword(request.getConfirmPassword());
        // 5️⃣ Also save in Register table
        User registerUser = new User();
        registerUser.setJuryId(savedUser.getId());  // link to Jury
        registerUser.setUsername(request.getUsername());
        registerUser.setPassword(passwordEncoder.encode(request.getPassword()));
        registerUser.setConfirmPassword(request.getConfirmPassword());
        if ("judge".equalsIgnoreCase(request.getRole())) {

            Role userRole = roleRep.findByName("JUDGE")
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            registerUser.setRole(userRole);    // store role name
          //  newUser.setRole(userRole);
        }


        //registerUser.setRole(userRole.getName());    // store role name

        authRep.save(registerUser);
       // return savedUser;
        return new ResponseDto(
                savedUser.getId(),
                savedUser.getName(),
                registerUser.getUsername(),
                registerUser.getRole().getName(),
                savedUser.getAddress(),
                savedUser.getDesignation(),
                registerUser.getRole().getId()
        );
       // return juryRep.save(newUser);
    }

    public ResponseDto updateUser(RequestDto request, Long id) {

        Jury user = juryRep.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));


        user.setAddress(request.getAddress());
        user.setDesignation(request.getDesignation());
        user.setName(request.getName());
       // user.setUsername(request.getUsername());
//        if(request.getPassword()!=null) {
//
//
//            user.setPassword(passwordEncoder.encode(request.getPassword()));
//            user.setConfirmPassword(request.getConfirmPassword());
//        }
//        Role role = roleRep.findByName(request.getRole())
//                .orElseThrow(() -> new RuntimeException("Role not found"));
//
//        user.setRole(role);
        Jury updatedUser = juryRep.save(user);
        User registerUser = authRep.findByJuryId(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("Register entry not found"));

        registerUser.setUsername(request.getUsername());
        if (request.getPassword() != null) {
                        registerUser.setPassword(passwordEncoder.encode(request.getPassword()));
            registerUser.setConfirmPassword(request.getConfirmPassword());
        }
        if(request.getRole()!=null){
            Role role = roleRep.findByName(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            registerUser.setRole(role);

        }


        authRep.save(registerUser);
        return new ResponseDto(
                updatedUser.getId(),
                updatedUser.getName(),
                registerUser.getUsername(),
                registerUser.getRole().getName(),
                updatedUser.getAddress(),
                updatedUser.getDesignation(),
                registerUser.getRole().getId()
        );
      //  return juryRep.save(user);
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
