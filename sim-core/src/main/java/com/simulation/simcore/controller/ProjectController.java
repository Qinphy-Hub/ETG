package com.simulation.simcore.controller;

import com.simulation.simcore.entity.Project;
import com.simulation.simcore.entity.Result;
import com.simulation.simcore.entity.User;
import com.simulation.simcore.service.ProjectService;
import com.simulation.simcore.service.UserService;
import com.simulation.simcore.utils.ThreadTool;
import com.simulation.simcore.utils.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;
    private final Workspace workspace;
    @Autowired
    public ProjectController(ProjectService projectService, UserService userService, Workspace workspace) {
        this.projectService = projectService;
        this.userService = userService;
        this.workspace = workspace;
    }

    public int authorityOfProject(Project project) {
        if (project == null) return -1;
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        if (userId != project.getUserId()) return -1;
        return userId;
    }

    @PostMapping("/create")
    public Result<String> createProject(String wksName, String wksDesc, String wksType, String proType) {
        if (wksName == null || wksName.isEmpty()) return Result.failure("project name empty!");
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        User user = userService.getUserInfo(userId);
        if (user == null) return Result.failure("User not found!");
        // create in database
        Project project = new Project();
        project.setWksName(wksName);
        project.setWksDesc(wksDesc);
        project.setUserId(userId);
        project.setProType(proType);
        project.setWksType(wksType);
        projectService.createProject(project);
        int id = project.getId();
        // create workspace
        if (workspace.checkWorkspaceDir(userId, id)) return Result.success("Some warning in workspace!");
        return Result.success("Project created!");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteProject(@PathVariable int id) {
        Project project = projectService.getProject(id);
        int userId = authorityOfProject(project);
        if (userId == -1) return Result.failure("User does not have the authority of project!");
        // delete project
        projectService.deleteProject(id);
        // delete from filesystem
        workspace.deleteWorkspaceDir(userId, id);
        return Result.success("Project deleted!");
    }

    @PatchMapping("/update")
    public Result<String> updateProject(int id, String wksName, String wksDesc) {
        Project project = projectService.getProject(id);
        int userId = authorityOfProject(project);
        if (userId == -1) return Result.failure("User does not have the authority of project!");
        // update
        projectService.updateProjectInfo(id, wksName, wksDesc);
        return Result.success("Project information updated!");
    }

    @GetMapping("/info/{id}")
    public Result<Project> getProjectInfo(@PathVariable int id) {
        Project project = projectService.getProject(id);
        int userId = authorityOfProject(project);
        if (userId == -1) return Result.failure("User does not have the authority of project!", null);
        return Result.success(project);
    }

    @GetMapping("/list")
    public Result<List<Project>> getAllProjects() {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        User user = userService.getUserInfo(userId);
        if (user == null) return Result.failure("user not found!", null);
        // get project list
        List<Project> projects = projectService.getProjectList(userId);
        return Result.success(projects);
    }

    @PatchMapping("/coordinate")
    public Result<String> updateCoordinate(int id, Double latitude, Double longitude) {
        Project project = projectService.getProject(id);
        int userId = authorityOfProject(project);
        if (userId == -1) return Result.failure("User does not have the authority of project!");
        // update
        projectService.updateProjectCoordinate(id, latitude, longitude);
        return Result.success("Project set coordinate successfully!");
    }

    @PatchMapping("/timer")
    public Result<String> updateTimer(int id, int granularity, String timeUnit, int timeLen) {
        Project project = projectService.getProject(id);
        int userId = authorityOfProject(project);
        if (userId == -1) return Result.failure("User does not have the authority of project!");
        // update
        projectService.updateTimer(id, granularity, timeUnit, timeLen);
        return Result.success("Project set coordinate successfully!");
    }
}
