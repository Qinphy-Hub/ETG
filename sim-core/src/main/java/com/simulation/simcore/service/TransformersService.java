package com.simulation.simcore.service;

import com.simulation.simcore.entity.Transformers;

import java.util.List;

public interface TransformersService {
    List<Transformers> getTransformersList();

    Transformers getTransformersById(int id);
}
