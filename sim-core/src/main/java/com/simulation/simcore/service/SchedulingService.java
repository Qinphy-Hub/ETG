package com.simulation.simcore.service;

import com.simulation.simcore.entity.Scheduling;

import java.util.List;

public interface SchedulingService {
    void createScheduling(Scheduling project);
    void deleteScheduling(int id);
    void updateSchedulingInfo(int id, String name, String carColor, String routeColor, boolean isActive);
    Scheduling getScheduling(int id);
    List<Scheduling> getSchedulingList(int wksId);
}
