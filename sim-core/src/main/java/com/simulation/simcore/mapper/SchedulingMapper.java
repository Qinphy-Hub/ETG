package com.simulation.simcore.mapper;

import com.simulation.simcore.entity.Scheduling;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SchedulingMapper {
    @Insert("INSERT INTO scheduling(wks_id, name, car_color, route_color, is_active) VALUES (#{wksId},#{name},#{carColor}, #{routeColor}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertScheduling(Scheduling project);

    @Delete("DELETE FROM scheduling WHERE id=#{id}")
    void deleteScheduling(int id);

    @Update("UPDATE scheduling SET name=#{name}, car_color=#{carColor}, route_color=#{routeColor}, is_active=#{isActive} WHERE id=#{id}")
    void updateSchedulingInfo(int id, String name, String carColor, String routeColor, boolean isActive);

    @Select("SELECT * FROM scheduling WHERE id=#{id}")
    Scheduling getSchedulingById(int id);

    @Select("SELECT * FROM scheduling WHERE wks_id=#{wksId}")
    List<Scheduling> getSchedulingList(int userId);
}