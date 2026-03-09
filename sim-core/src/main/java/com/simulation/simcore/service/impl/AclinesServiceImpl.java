package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.Aclines;
import com.simulation.simcore.mapper.AclinesMapper;
import com.simulation.simcore.service.AclinesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AclinesServiceImpl implements AclinesService {
    private final AclinesMapper aclinesMapper;
    @Autowired
    public AclinesServiceImpl(AclinesMapper aclinesMapper) {
        this.aclinesMapper = aclinesMapper;
    }

    @Override
    public List<Aclines> getAclinesList() {
        return aclinesMapper.findAll();
    }

    @Override
    public Aclines getAclinesById(int id) {
        return aclinesMapper.findById(id);
    }
}
