package com.simulation.simcore.service;

import com.simulation.simcore.entity.Aclines;

import java.util.List;

public interface AclinesService {
    List<Aclines> getAclinesList();
    Aclines getAclinesById(int id);
}
