package com.simulation.simcore.mapper;

import com.simulation.simcore.entity.Flp;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FlpMapper {
    @Insert("INSERT INTO flp(wks_id, name, color, is_active) VALUES (#{wksId}, #{name}, #{color}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertFlp(Flp flp);

    @Delete("DELETE FROM flp WHERE id=#{id}")
    void deleteFlp(int id);

    @Update("UPDATE flp SET name=#{name}, color=#{color}, is_active=#{isActive} WHERE id=#{id}")
    void updateFlp(int id, String name, String color, boolean isActive);

    @Select("SELECT * FROM flp WHERE id=#{id}")
    Flp getFlpById(int id);

    @Select("SELECT * FROM flp WHERE wks_id=#{wksId}")
    List<Flp> getFlpList(int wksId);
}
