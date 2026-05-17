package com.example.documentsAPP.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LearningResultUpdateRequest {
    private String subjectCode;
    private String subjectName;
    private Integer number;
    private String description;
    private Long courseId;
}