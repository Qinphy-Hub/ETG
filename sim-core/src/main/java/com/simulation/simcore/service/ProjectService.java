package com.simulation.simcore.service;

import com.simulation.simcore.entity.Project;

import java.util.List;

public interface ProjectService {
    void createProject(Project project);
    void deleteProject(int id);
    void updateProjectInfo(int id, String wksName, String wksDesc);
    void updateProjectCoordinate(int id, Double latitude, Double longitude);
    void updateTimer(int id, int granularity, String timeUnit, int timeLen);
    Project getProject(int id);
    List<Project> getProjectList(int userId);
}
