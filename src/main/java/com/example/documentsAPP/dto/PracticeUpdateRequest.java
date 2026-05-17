package com.example.documentsAPP.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class PracticeUpdateRequest {

    private Long companyId;
    private Long traineeId;
    private List<Long> studentIds;

    private String workplace;
    private LocalDate startDate;
    private LocalDate endDate;
    private String schedule;

    private Integer totalHours;
    private Integer dailyHours;
    private LocalTime startTime;
    private LocalTime endTime;
}