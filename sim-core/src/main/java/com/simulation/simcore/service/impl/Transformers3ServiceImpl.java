package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.Transformers3;
import com.simulation.simcore.mapper.Transformers3Mapper;
import com.simulation.simcore.service.Transformers3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Transformers3ServiceImpl implements Transformers3Service {
    private Transformers3Mapper transformers3Mapper;
    @Autowired
    public void setTransformers3Mapper(Transformers3Mapper transformers3Mapper) {
        this.transformers3Mapper = transformers3Mapper;
    }

    @Override
    public List<Transformers3> getTransformers3List() {
        return transformers3Mapper.getAllTransformers3();
    }

    @Override
    public Transformers3 getTransformers3ById(int id) {
        return transformers3Mapper.getTransformers3(id);
    }
}
