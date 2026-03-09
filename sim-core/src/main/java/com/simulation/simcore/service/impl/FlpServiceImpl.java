package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.Flp;
import com.simulation.simcore.mapper.FlpMapper;
import com.simulation.simcore.service.FlpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlpServiceImpl implements FlpService {
    private FlpMapper flpMapper;
    @Autowired
    public void setFlpMapper(FlpMapper flpMapper) {
        this.flpMapper = flpMapper;
    }

    @Override
    public void createFlp(Flp flp) {
        flpMapper.insertFlp(flp);
    }

    @Override
    public void deleteFlp(int id) {
        flpMapper.deleteFlp(id);
    }

    @Override
    public void updateFlpInfo(int id, String name, String color, boolean isActive) {
        flpMapper.updateFlp(id, name, color, isActive);
    }

    @Override
    public Flp getFlp(int id) {
        return flpMapper.getFlpById(id);
    }

    @Override
    public List<Flp> getFlpList(int wksId) {
        return flpMapper.getFlpList(wksId);
    }
}
