package com.example.school.jury;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDto {

        private Long juryId;
        private String name;
        private String username;
        private String rolename;
        private Long roleId;
        private String address;
        private String designation;

        // Constructors
        public ResponseDto(Long juryId, String name, String username, String role,
                               String address, String designation,Long roleId) {
            this.juryId = juryId;
            this.name = name;
            this.username = username;
            this.rolename = role;
            this.roleId=roleId;
            this.address = address;
            this.designation = designation;
        }

        // Getters & Setters
    }


