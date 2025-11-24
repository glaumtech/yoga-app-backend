package com.example.school.participants;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ParticipantsDto {
    private Long id;

    private String participantName;

    private LocalDate dateOfBirth;
    private Long age;
    private String gender;
    private String category;
    private String schoolName;
    private String standard;
    private String yogaMasterName;
    private String status;
    private Long yogaMasterContact;
    private String address;
    private String participantCode;
    private Long eventId;

    private String photo; // to store image filename

    public ParticipantsDto(Participants participants){
        this.id= participants.getId();;
        this.participantName=participants.getParticipantName();
        this.dateOfBirth=participants.getDateOfBirth();
        this.age=participants.getAge();
        this.gender=participants.getGender();
        this.category=participants.getCategory();
        this.eventId=participants.getEventId();
        this.participantCode=participants.getParticipantCode();
        this.schoolName=participants.getSchoolName();
        this.standard=participants.getStandard();
        this.yogaMasterContact=participants.getYogaMasterContact();
        this.yogaMasterName=participants.getYogaMasterName();
        this.status=participants.getStatus();
        this.address=participants.getAddress();
        this.photo=participants.getPhoto();
    }
}
