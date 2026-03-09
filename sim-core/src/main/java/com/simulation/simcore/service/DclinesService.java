package com.simulation.simcore.service;

import com.simulation.simcore.entity.Dclines;

import java.util.List;

public interface DclinesService {
    List<Dclines> getDclinesList();
    Dclines getDclinesById(int id);
}
