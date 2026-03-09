package com.simulation.simcore.entity;

import lombok.Data;

@Data
public class Transformers {
    private int id;
    private String name;
    private Double snMva;
    private Double vnHvKv;
    private Double vnLvKv;
    private Double vkPercent;
    private Double vkrPercent;
    private Double pfeKw;
    private Double i0Percent;
    private Double shiftDegree;
    private String tapSide;
    private int tapNeutral;
    private int tapMin;
    private int tapMax;
    private Double tapStepPercent;
    private Double tapStepDegree;
    private String tapChangerType;
}
