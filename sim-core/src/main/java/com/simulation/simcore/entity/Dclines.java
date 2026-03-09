package com.simulation.simcore.entity;

import lombok.Data;

@Data
public class Dclines {
    private int id;
    private String name;
    private Double rOhmPerKm;
    private Double MaxIKa;
    private String type;
    private Double qMm2;
    private Double alpha;
}
