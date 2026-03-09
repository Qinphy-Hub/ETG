package com.simulation.simcore.mapper;

import com.simulation.simcore.entity.Aclines;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AclinesMapper {
    @Select("SELECT * FROM aclines WHERE id=#{id}")
    public Aclines findById(int id);

    @Select("SELECT * FROM aclines")
    public List<Aclines> findAll();
}
