package com.simulation.simcore.mapper;

import com.simulation.simcore.entity.Transformers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransformersMapper {
    @Select("SELECT * FROM transformers WHERE id=#{id}")
    public Transformers findById(int id);

    @Select("SELECT * FROM transformers")
    public List<Transformers> findAll();
}
