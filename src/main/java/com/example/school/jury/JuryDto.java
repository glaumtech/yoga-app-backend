package com.example.school.jury;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JuryDto {
    private Long id;
    private String name;
    public JuryDto(Jury jury) {
        this.id = jury.getId();
        this.name = jury.getName();
    }
    public JuryDto(Long id,String name){
        this.id=id;
        this.name=name;
    }

    public JuryDto(){

    }
}
