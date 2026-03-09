package com.simulation.simcore.service;

import com.simulation.simcore.entity.Flp;

import java.util.List;

public interface FlpService {
    void createFlp(Flp flp);
    void deleteFlp(int id);
    void updateFlpInfo(int id, String name, String color, boolean isActive);
    Flp getFlp(int id);
    List<Flp> getFlpList(int wksId);
}
