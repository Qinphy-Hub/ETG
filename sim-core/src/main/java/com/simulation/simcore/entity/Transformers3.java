package com.simulation.simcore.entity;

import lombok.Data;

@Data
public class Transformers3 {
    private int id;
    private String name;
    private Double snHvMva;
    private Double snMvMva;
    private Double snLvMva;
    private Double vnHvKv;
    private Double vnMvKv;
    private Double vnLvKv;
    private Double vkHvPercent;
    private Double vkMvPercent;
    private Double vkLvPercent;
    private Double vkrHvPercent;
    private Double vkrMvPercent;
    private Double vkrLvPercent;
    private Double pfeKw;
    private Double i0Percent;
    private Double shiftMvDegree;
    private Double shiftLvDegree;
    private String tapSide;
    private Double tapPercent;
    private int tapNeutral;
    private int tapMin;
    private int tapMax;
    private Double tapStepPercent;
    private String tapChangerType;
}
