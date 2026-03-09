package com.simulation.simcore.mapper;

import com.simulation.simcore.entity.Dclines;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DclinesMapper {
    @Select("SELECT * FROM dclines WHERE id=#{id}")
    public Dclines findById(int id);

    @Select("SELECT * FROM dclines")
    public List<Dclines> findAll();
}
