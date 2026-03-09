package com.simulation.simcore.entity;

import lombok.Data;

@Data
public class Scheduling {
    private int id;
    private int wksId;
    private String name;
    private String carColor;
    private String routeColor;
    private boolean isActive;
}
