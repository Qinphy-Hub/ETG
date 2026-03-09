package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.Project;
import com.simulation.simcore.mapper.ProjectMapper;
import com.simulation.simcore.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectMapper projectMapper;
    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public void createProject(Project project) {
        projectMapper.insertProject(project);
    }

    @Override
    public void deleteProject(int id) {
        projectMapper.deleteProject(id);
    }

    @Override
    public void updateProjectInfo(int id, String wksName, String wksDesc) {
        projectMapper.updateProjectInfo(id, wksName, wksDesc);
    }

    @Override
    public void updateProjectCoordinate(int id, Double latitude, Double longitude) {
        projectMapper.updateProjectCoordinate(id, latitude, longitude);
    }

    @Override
    public void updateTimer(int id, int granularity, String timeUnit, int timeLen) {
        projectMapper.updateProjectTimer(id, granularity, timeUnit, timeLen);
    }

    @Override
    public Project getProject(int id) {
        return projectMapper.getProjectById(id);
    }

    @Override
    public List<Project> getProjectList(int userId) {
        return projectMapper.getProjectList(userId);
    }
}
