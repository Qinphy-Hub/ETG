package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.Scheduling;
import com.simulation.simcore.mapper.SchedulingMapper;
import com.simulation.simcore.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchedulingServiceImpl implements SchedulingService {
    private SchedulingMapper schedulingMapper;
    @Autowired
    public void setProjectMapper(SchedulingMapper schedulingMapper) {
        this.schedulingMapper = schedulingMapper;
    }

    @Override
    public void createScheduling(Scheduling project) {
        schedulingMapper.insertScheduling(project);
    }

    @Override
    public void deleteScheduling(int id) {
        schedulingMapper.deleteScheduling(id);
    }

    @Override
    public void updateSchedulingInfo(int id, String name, String carColor, String routeColor, boolean isActive) {
        schedulingMapper.updateSchedulingInfo(id, name, carColor, routeColor, isActive);
    }

    @Override
    public Scheduling getScheduling(int id) {
        return schedulingMapper.getSchedulingById(id);
    }

    @Override
    public List<Scheduling> getSchedulingList(int wksId) {
        return schedulingMapper.getSchedulingList(wksId);
    }
}
