package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.Transformers;
import com.simulation.simcore.mapper.TransformersMapper;
import com.simulation.simcore.service.TransformersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransformersServiceImpl implements TransformersService {
    private TransformersMapper transformersMapper;
    @Autowired
    public void setTransformersMapper(TransformersMapper transformersMapper) {
        this.transformersMapper = transformersMapper;
    }

    @Override
    public List<Transformers> getTransformersList() {
        return transformersMapper.findAll();
    }

    @Override
    public Transformers getTransformersById(int id) {
        return transformersMapper.findById(id);
    }
}
