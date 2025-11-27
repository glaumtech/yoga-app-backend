package com.example.school.participants;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class PageFilterRequest {

    private ParticipantFilter filter;
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortDirection = "asc";

    public Pageable toPageable() {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
