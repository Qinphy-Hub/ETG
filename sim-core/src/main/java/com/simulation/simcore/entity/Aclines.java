package com.simulation.simcore.entity;

import lombok.Data;

@Data
public class Aclines {
    private int id;
    private String name;
    private Double rOhmPerKm;
    private Double xOhmPerKm;
    private Double cNfPerKm;
    private Double maxIKa;
    private String type;
    private Double qMm2;
    private Double alpha;
}
