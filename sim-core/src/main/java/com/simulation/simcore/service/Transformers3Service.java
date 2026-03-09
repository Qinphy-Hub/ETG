package com.simulation.simcore.service;

import com.simulation.simcore.entity.Transformers;
import com.simulation.simcore.entity.Transformers3;

import java.util.List;

public interface Transformers3Service {
    List<Transformers3> getTransformers3List();

    Transformers3 getTransformers3ById(int id);
}
