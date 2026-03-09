package com.simulation.simcore.mapper;

import com.simulation.simcore.entity.Transformers3;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface Transformers3Mapper {
    @Select("SELECT * FROM transformers3 WHERE id=#{id}")
    public Transformers3 getTransformers3(int id);

    @Select("SELECT * FROM transformers3")
    public List<Transformers3> getAllTransformers3();
}
