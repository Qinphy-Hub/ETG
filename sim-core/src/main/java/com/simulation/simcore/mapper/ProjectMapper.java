package com.simulation.simcore.mapper;

import com.simulation.simcore.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMapper {
    @Insert("INSERT INTO workspace(user_id, wks_name, wks_desc, wks_type, pro_type, create_date) VALUES (#{userId},#{wksName},#{wksDesc}, #{wksType}, #{proType},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertProject(Project project);

    @Delete("DELETE FROM workspace WHERE id=#{id}")
    void deleteProject(int id);

    @Update("UPDATE workspace SET wks_name=#{wksName}, wks_desc=#{wksDesc} WHERE id=#{id}")
    void updateProjectInfo(int id, String wksName, String wksDesc);

    @Update("UPDATE workspace SET latitude=#{latitude}, longitude=#{longitude} WHERE id=#{id}")
    void updateProjectCoordinate(int id, Double latitude, Double longitude);

    @Update("UPDATE workspace SET granularity=#{granularity}, time_unit=#{timeUnit}, time_len=#{timeLen} WHERE id=#{id}")
    void updateProjectTimer(int id, int granularity, String timeUnit, int timeLen);

    @Select("SELECT * FROM workspace WHERE id=#{id}")
    Project getProjectById(int id);

    @Select("SELECT * FROM workspace WHERE user_id=#{userId}")
    List<Project> getProjectList(int userId);
}
