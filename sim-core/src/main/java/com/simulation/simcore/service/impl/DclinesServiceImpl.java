package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.Dclines;
import com.simulation.simcore.mapper.DclinesMapper;
import com.simulation.simcore.service.DclinesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DclinesServiceImpl implements DclinesService {
    private DclinesMapper dclinesMapper;
    @Autowired
    public void setDclinesMapper(DclinesMapper dclinesMapper) {
        this.dclinesMapper = dclinesMapper;
    }

    @Override
    public List<Dclines> getDclinesList() {
        return dclinesMapper.findAll();
    }

    @Override
    public Dclines getDclinesById(int id) {
        return dclinesMapper.findById(id);
    }
}
