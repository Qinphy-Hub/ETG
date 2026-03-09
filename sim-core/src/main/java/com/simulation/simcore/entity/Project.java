package com.simulation.simcore.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class Project {
    private int id;
    private int userId;
    private String wksName;
    private String wksDesc;
    private String wksType;
    private String proType;
    private Double latitude;
    private Double longitude;
    private int granularity;
    private String timeUnit;
    private int timeLen;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createDate;
}
