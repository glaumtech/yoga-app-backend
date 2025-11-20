package com.example.school.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RequestDto {
    private Long id;

    private String title;
    private String description;
    private String venue;
    private String venueAddress;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fileName;
    private boolean active ;
    private boolean current ;
}
