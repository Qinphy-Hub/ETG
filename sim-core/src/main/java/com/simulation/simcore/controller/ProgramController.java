package com.simulation.simcore.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.simulation.simcore.entity.*;
import com.simulation.simcore.service.FlpService;
import com.simulation.simcore.service.ProjectService;
import com.simulation.simcore.service.SchedulingService;
import com.simulation.simcore.service.UserService;
import com.simulation.simcore.utils.ThreadTool;
import com.simulation.simcore.utils.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/program")
public class ProgramController {
    private final Workspace workspace;
    private final UserService userService;
    private final SchedulingService schedulingService;
    private final ProjectService projectService;
    private final FlpService flpService;
    @Autowired
    public ProgramController(Workspace workspace, UserService userService, SchedulingService schedulingService, ProjectService projectService, FlpService flpService) {
        this.workspace = workspace;
        this.userService = userService;
        this.schedulingService = schedulingService;
        this.projectService = projectService;
        this.flpService = flpService;
    }

    private int userAuthorization() {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        User user = userService.getUserInfo(userId);
        if (user == null) return -1;
        return userId;
    }



    // --------------------------------------- Program Manage -------------------------------------
    // 1. One Car Dispatching
    @PostMapping("/route-list")
    public Result<List<Scheduling>> getSchedulingList(int projectId) {
        if (userAuthorization() == -1) return Result.failure("User not found!", null);
        // get project list
        List<Scheduling> projects = schedulingService.getSchedulingList(projectId);
        return Result.success(projects);
    }

    @GetMapping("/route-info/{id}")
    public Result<Scheduling> getProjectInfo(@PathVariable int id) {
        if (userAuthorization() == -1) return Result.failure("User not found!", null);
        Scheduling project = schedulingService.getScheduling(id);
        return Result.success(project);
    }

    @DeleteMapping("/route/{id}")
    public Result<String> deleteProject(@PathVariable int id) {
        if (userAuthorization() == -1) return Result.failure("User not found!");
        // delete project
        schedulingService.deleteScheduling(id);
        return Result.success("Project deleted!");
    }

    @PatchMapping("/route-update")
    public Result<String> updateProject(int id, String name, String carColor, String routeColor, boolean active) {
        if (userAuthorization() == -1) return Result.failure("User not found!");
        // update
        schedulingService.updateSchedulingInfo(id, name, carColor, routeColor, active);
        return Result.success("Project information updated!");
    }

    @PostMapping("/route-create")
    public Result<String> createProject(int projectId, String name, String carColor, String routeColor, boolean active) {
        if (name.isEmpty() || carColor.isEmpty() || routeColor.isEmpty()) return Result.failure("program setting empty!");
        if (userAuthorization() == -1) return Result.failure("User not found!");
        // create in database
        Scheduling project = new Scheduling();
        project.setName(name);
        project.setCarColor(carColor);
        project.setRouteColor(routeColor);
        project.setActive(active);
        project.setWksId(projectId);
        schedulingService.createScheduling(project);
        return Result.success("Program created!");
    }

    // 2. FLP Program
    @PostMapping("/flp-create")
    public Result<String> createFlp(int projectId, String name, String color, boolean active) {
        if (name.isEmpty() || color.isEmpty()) return Result.failure("program setting empty!");
        if (userAuthorization() == -1) return Result.failure("User not found!");
        Flp flp = new Flp();
        flp.setName(name);
        flp.setColor(color);
        flp.setActive(active);
        flp.setWksId(projectId);
        flpService.createFlp(flp);
        return Result.success("Flp created!");
    }

    @PostMapping("/flp-list")
    public Result<List<Flp>> getFlpList(int projectId) {
        if (userAuthorization() == -1) return Result.failure("User not found!", null);
        List<Flp> flpList = flpService.getFlpList(projectId);
        return Result.success(flpList);
    }

    @PatchMapping("/flp-update")
    public Result<String> updateFlp(int id, String name, String color, boolean active) {
        if (userAuthorization() == -1) return Result.failure("User not found!");
        flpService.updateFlpInfo(id, name, color, active);
        return Result.success("Flp updated!");
    }

    @DeleteMapping("/flp/{id}")
    public Result<String> deleteFlp(@PathVariable int id) {
        if (userAuthorization() == -1) return Result.failure("User not found!");
        flpService.deleteFlp(id);
        return Result.success("Flp deleted!");
    }

    @GetMapping("/flp-info/{id}")
    public Result<Flp> getFlpInfo(@PathVariable int id) {
        if (userAuthorization() == -1) return Result.failure("User not found!", null);
        Flp flp = flpService.getFlp(id);
        return Result.success(flp);
    }
    // --------------------------------------- Program Manage -------------------------------------



    // --------------------------------------- Program Editor -------------------------------------
    private String getCodeFromWorkspace(String programName, String programType, int projectId, int programId) {
        String fileName = programName + "_" + programId + ".py";
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        if (workspace.checkTmpDir(userId, projectId)) return null;
        String basePath = workspace.getTmpDir(userId, projectId);
        File file = new File(basePath + "/" + fileName);
        try {
            byte[] bytes;
            String content;
            if (!file.exists()) {
                content = workspace.getTemplateCode(programType, programName + ".py");
            } else {
                bytes = Files.readAllBytes(Paths.get(basePath + "/" + fileName));
                content = new String(bytes, StandardCharsets.UTF_8);
            }
            return content;
        } catch (IOException e) {
            return null;
        }
    }
    // save program
    private boolean saveCodeAtWorkspace(int projectId, String fileName, String code) {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        if (workspace.checkTmpDir(userId, projectId)) return false;
        String basePath = workspace.getTmpDir(userId, projectId);
        workspace.saveDataToFile(basePath + "/" + fileName, code);
        return true;
    }
    // initial program
    private String initializeCode(int projectId, String programType, String templateFileName, String saveFileName) {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        if (workspace.checkTmpDir(userId, projectId)) return null;
        String basePath = workspace.getTmpDir(userId, projectId);
        String content = workspace.getTemplateCode(programType, templateFileName);
        workspace.saveDataToFile(basePath + "/" + saveFileName, content);
        return content;
    }

    // 1. one car scheduling program
    @PostMapping("/code-route")
    public Result<String> getDefaultRoutes(int projectId, int programId) {
        String content = getCodeFromWorkspace("route", "route", projectId, programId);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    @PostMapping("/save-route")
    public Result<String> saveDefaultRoutes(int projectId, int programId, String code) {
        String fileName = "route_" + programId + ".py";
        boolean f = saveCodeAtWorkspace(projectId, fileName, code);
        if (!f) return Result.failure("File system error!");
        return Result.success();
    }

    @PostMapping("/init-route")
    public Result<String> initialDefaultRoutes(int projectId, int programId) {
        String fileName = "route_" + programId + ".py";
        String content = initializeCode(projectId, "route", "route.py", fileName);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    // 2. FLP program
    @PostMapping("/code-flp")
    public Result<String> getDefaultFlp(int projectId, int programId) {
        String content = getCodeFromWorkspace("flp_solver", "flp", projectId, programId);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    @PostMapping("/save-flp")
    public Result<String> saveDefaultFlp(int projectId, int programId, String code) {
        String fileName = "flp_solver_" + programId + ".py";
        boolean f = saveCodeAtWorkspace(projectId, fileName, code);
        if (!f) return Result.failure("File system error!");
        return Result.success();
    }

    @PostMapping("/init-flp")
    public Result<String> initDefaultFlp(int projectId, int programId) {
        String fileName = "flp_solver_" + programId + ".py";
        String templateFileName = "flp_solver.py";
        String content = initializeCode(projectId, "flp", templateFileName, fileName);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    // 2. FLP program: covered demands
    @PostMapping("/code-covered-demands")
    public Result<String> getCodeCoveredDemands(int projectId) {
        String content = getCodeFromWorkspace("covered_demands", "flp", projectId, 0);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    @PostMapping("/save-covered-demands")
    public Result<String> saveCodeCoveredDemands(int projectId, String code) {
        String fileName = "covered_demands_0.py";
        boolean f = saveCodeAtWorkspace(projectId, fileName, code);
        if (!f) return Result.failure("File system error!");
        return Result.success();
    }

    @PostMapping("/init-covered-demands")
    public Result<String> initCodeCoveredDemands(int projectId) {
        String fileName = "covered_demands_0.py";
        String templateFileName = "covered_demands.py";
        String content = initializeCode(projectId, "flp", templateFileName, fileName);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    // calculate vehicle speed
    @PostMapping("/code-speed")
    public Result<String> getFlowSpeedCode(int projectId) {
        String content = getCodeFromWorkspace("vehicle_speed", "route", projectId, 0);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    @PostMapping("/save-speed")
    public Result<String> saveFlowSpeedCode(int projectId, String code) {
        boolean f = saveCodeAtWorkspace(projectId, "vehicle_speed_0.py", code);
        if (!f) return Result.failure("File system error!");
        return Result.success();
    }

    @PostMapping("/init-speed")
    public Result<String> initialFlowSpeedCode(int projectId) {
        String content = initializeCode(projectId, "route", "vehicle_speed.py", "vehicle_speed_0.py");
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    // calculate edge's traffic flow
    @PostMapping("/code-flow")
    public Result<String> getFlowCode(int projectId) {
        String content = getCodeFromWorkspace("traffic_flow", "route", projectId, 0);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    @PostMapping("/save-flow")
    public Result<String> saveFlowCode(int projectId, String code) {
        boolean f = saveCodeAtWorkspace(projectId, "traffic_flow_0.py", code);
        if (!f) return Result.failure("File system error!");
        return Result.success();
    }

    @PostMapping("/init-flow")
    public Result<String> initialFlowCode(int projectId) {
        String content = initializeCode(projectId, "route", "traffic_flow.py", "traffic_flow_0.py");
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    // give some noise into the original traffic flow data
    @PostMapping("/code-noise-flow")
    public Result<String> getNoiseFlowCode(int projectId) {
        String content = getCodeFromWorkspace("noise_flow", "route", projectId, 0);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    @PostMapping("/save-noise-flow")
    public Result<String> saveNoiseFlowCode(int projectId, String code) {
        boolean f = saveCodeAtWorkspace(projectId, "noise_flow_0.py", code);
        if (!f) return Result.failure("File system error!");
        return Result.success();
    }

    @PostMapping("/init-noise-flow")
    public Result<String> initialNoiseFlowCode(int projectId) {
        String content = initializeCode(projectId, "route", "noise_flow.py", "noise_flow_0.py");
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    // set distribution
    @PostMapping("/code-distribution")
    public Result<String> getDistributionCode(int projectId) {
        String content = getCodeFromWorkspace("distribution", "route", projectId, 0);
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }

    @PostMapping("/save-distribution")
    public Result<String> saveDistributionCode(int projectId, String code) {
        boolean f = saveCodeAtWorkspace(projectId, "distribution_0.py", code);
        if (!f) return Result.failure("File system error!");
        return Result.success();
    }

    @PostMapping("/init-distribution")
    public Result<String> initialDistributionCode(int projectId) {
        String content = initializeCode(projectId, "route", "distribution.py", "distribution_0.py");
        if (null == content) return Result.failure("File system error!");
        return Result.success(content);
    }
    // --------------------------------------- Program Editor -------------------------------------



    // -------------------------------------- Main Run Program ------------------------------------
    // run program tool
    private Map<String, String> runProgramMain(String basePath, String[] args) {
        Map<String, String> data = new HashMap<>();
        try {
            Process p = Runtime.getRuntime().exec(args, null, new File(basePath));
            // result
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String flow = reader.readLine();
            // error message
            BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String res = null;
            StringBuilder err = new StringBuilder();
            while ((res = errReader.readLine()) != null) {
                err.append(res).append("\n");
            }
            data.put("err", err.toString());
            data.put("res", flow);
            reader.close();
            errReader.close();
            p.waitFor();
            p.destroy();
            return data;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    @PostMapping("/calc/flp")
    public Result<Map<Integer, JSONObject>> calcFlp(int projectId) {
        int userId = userAuthorization();
        if (userId == -1) return Result.failure("User not found!", null);
        if (workspace.checkResultDir(userId, projectId)) return Result.failure("File System Error!", null);
        List<Flp> flpList = flpService.getFlpList(projectId);
        if (flpList == null) return Result.failure("Project empty!", null);
        String main_content = workspace.getTemplateCode("flp", "main_flp.py");
        Map<Integer, JSONObject> data = new HashMap<>();
        for (Flp flp : flpList) {
            String solver_content = null;
            int programId = flp.getId();
            if (flp.isActive()) {
                solver_content = getCodeFromWorkspace("flp_solver", "flp", projectId, programId);
                String content = solver_content + main_content;
                String basePath = workspace.getTmpDir(userId, projectId);
                String mainFileName = "main_flp-" + programId + ".py";
                workspace.saveDataToFile(basePath + "/" + mainFileName, content);
                String[] args = {"python", mainFileName, "--id", String.valueOf(programId)};
                Map<String, String> json = runProgramMain(basePath, args);
                if (null == json) return  Result.failure("run program failed!", null);
                if (json.get("res") == null) {
                    return Result.failure(json.get("err"), null);
                }
                data.put(programId, JSON.parseObject(json.get("res")));
            }
        }
        return Result.success(data);
    }
    // -------------------------------------- Main Run Program ------------------------------------










    /** simulation **/



    private Map<Integer, JSONObject> simSingleScheduling(int userId, int projectId) throws IOException {
        // basic core function
        Resource resource = new ClassPathResource("singleMain.py");
        String basePath = workspace.getWorkspaceDir(userId, projectId);
        String resourcePath = resource.getFile().getPath();
        byte[] bytes = Files.readAllBytes(Paths.get(resourcePath));
        String origin = new String(bytes, StandardCharsets.UTF_8);

        // project information
        Project project = projectService.getProject(projectId);
        int granularity = project.getGranularity();
        int time_len = project.getTimeLen();

        Map<Integer, JSONObject> data = new HashMap<>();
        List<Scheduling> programs = schedulingService.getSchedulingList(projectId);
        for (Scheduling scheduling : programs) {
            boolean active = scheduling.isActive();
            if (active) {
                int programId = scheduling.getId();
                String programName = "single-" + programId + ".py";
                // combine program
                String filePath = workspace.getWorkspaceDir(userId, projectId) + "/" + programName;
                byte[] user_bytes = Files.readAllBytes(Paths.get(filePath));
                String content = new String(user_bytes, StandardCharsets.UTF_8);
                String programFile = "singleMain-" + programId + ".py";
                workspace.saveDataToFile(basePath + "/" + programFile, content + origin);
                // run program
                String[] args = new String[] {"python", programFile, "--t", String.valueOf(time_len), "--g", String.valueOf(granularity)};
                Map<String, String> json = runProgramMain(basePath, args);
                if (null == json) return null;
                data.put(programId, JSON.parseObject(json.get("res")));
            }
        }
        return data;
    }

    @PostMapping("/simulation")
    public Result<Map<Integer, JSONObject>> simulation(int projectId) throws IOException {
        Project project = projectService.getProject(projectId);
        if (project == null) return Result.failure("Project not found!", null);
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        if (userId != project.getUserId()) return Result.failure("You do not have permission of this project!", null);
        // adjust program type
        String proType = project.getProType();
        Map<Integer, JSONObject> data = null;
        if (proType.equals("Single-Schedule")) {
            data = simSingleScheduling(userId, projectId);
        }
        return Result.success(data);
    }






    /** run program **/

    @PostMapping("/run/traffic-flow")
    public Result<Map<String, JSONObject>> runTrafficFlow(int projectId) {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        String filePath = workspace.getWorkspaceDir(userId, projectId) + "/modifyTrafficFlow.py";
        Resource resource = new ClassPathResource("runTrafficFlow.py");
        String basePath = workspace.getTmpDir(userId, projectId);
        try {
            byte[] user_bytes = Files.readAllBytes(Paths.get(filePath));
            String content = new String(user_bytes, StandardCharsets.UTF_8);
            String resourcePath = resource.getFile().getPath();
            byte[] bytes = Files.readAllBytes(Paths.get(resourcePath));
            String origin = new String(bytes, StandardCharsets.UTF_8);
            workspace.saveDataToFile(basePath + "/runTrafficFlow.py", content + origin);
        } catch (IOException e) {
            return Result.failure("File system error!", null);
        }
        String[] args = new String[] {"python", "runTrafficFlow.py"};
        Map<String, JSONObject> data = new HashMap<>();
        data.put("trafficFlow", null);
        try {
            Process p = Runtime.getRuntime().exec(args, null, new File(basePath));
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String flow = reader.readLine();
            System.out.println(flow);
            data.put("trafficFlow", JSONObject.parseObject(flow));
            return Result.success(data);
        } catch (IOException e) {
            return Result.failure("File system error!", null);
        }
    }

    @PostMapping("/run/scheduling")
    public Result<Map<String, JSONObject>> runScheduling(int projectId) {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        String filePath = workspace.getWorkspaceDir(userId, projectId) + "/scheduling.py";
        Resource resource = new ClassPathResource("runScheduling.py");
        String basePath = workspace.getTmpDir(userId, projectId);
        try {
            byte[] user_bytes = Files.readAllBytes(Paths.get(filePath));
            String content = new String(user_bytes, StandardCharsets.UTF_8);
            String resourcePath = resource.getFile().getPath();
            byte[] bytes = Files.readAllBytes(Paths.get(resourcePath));
            String origin = new String(bytes, StandardCharsets.UTF_8);
            workspace.saveDataToFile(basePath + "/runScheduling.py", content + origin);
        } catch (IOException e) {
            return Result.failure("File system error!", null);
        }
        String[] args = new String[] {"python", "runScheduling.py"};
        Map<String, JSONObject> data = new HashMap<>();
        data.put("scheduling", null);
        try {
            Process p = Runtime.getRuntime().exec(args, null, new File(basePath));
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String flow = reader.readLine();
            System.out.println(flow);
            data.put("scheduling", JSONObject.parseObject(flow));
            return Result.success(data);
        } catch (IOException e) {
            return Result.failure("File system error!", null);
        }
    }

    @PostMapping("/run/shortest-path")
    public Result<Map<String, JSONObject>> runShortestPath(int projectId, String start, String end) {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        String basePath = workspace.getTmpDir(userId, projectId);
        String[] args = new String[] {"python", "runTrafficFlow.py", "--start", start, "end", end};
        Map<String, JSONObject> data = new HashMap<>();
        data.put("shortestPath", null);
        try {
            Process p = Runtime.getRuntime().exec(args, null, new File(basePath));
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String flow = reader.readLine();
            System.out.println(flow);
            data.put("shortestPath", JSONObject.parseObject(flow));
            return Result.success(data);
        } catch (IOException e) {
            return Result.failure("File system error!", null);
        }
    }









//    /** set relation between traffic flow and vehicle speed. **/
//


//    /** set traffic flow calc algorithm. **/
//
//    // 1. get program
//    @PostMapping("/setTrafficFlow")
//    public Result<String> getSEtFlowMethod(int projectId) {
//        String content = getCodeFromWorkspace(projectId, "trafficFlowCalc.py");
//        if (null == content) return Result.failure("File system error!");
//        return Result.success(content);
//    }
//
//    // 2. save program
//    @PostMapping("/saveSetTrafficFlow")
//    public Result<String> saveSetFlowCode(int projectId, String code) {
//        boolean f = saveCodeAtWorkspace(projectId, "trafficFlowCalc.py", code);
//        if (!f) return Result.failure("File system error!");
//        return Result.success();
//    }
//
//    // 3. initial program by user
//    @PostMapping("/initialSetTrafficFlow")
//    public Result<String> initialSetTrafficFlow(int projectId) {
//        String content = initializeCode(projectId, "trafficFlowCalc.py");
//        if (null == content) return Result.failure("File system error!");
//        return Result.success(content);
//    }
//
//    /** modify traffic flow data by user **/
//
//    // get program
//    @PostMapping("/getModifyTrafficFlow")
//    public Result<String> getModifyTrafficFlow(int projectId) {
//        String content = getCodeFromWorkspace(projectId, "modifyTrafficFlow.py");
//        if (null == content) return Result.failure("File system error!");
//        return Result.success(content);
//    }
//
//    // save program
//    @PostMapping("/saveModifyTrafficFlow")
//    public Result<String> saveModifyTrafficFlow(int projectId, String code) {
//        boolean f = saveCodeAtWorkspace(projectId, "modifyTrafficFlow.py", code);
//        if (!f) return Result.failure("File system error!");
//        return Result.success();
//    }
//
//    // initial program by user
//    @PostMapping("/initialModifyTrafficFlow")
//    public Result<String> initialModifyTrafficFlow(int projectId) {
//        String content = initializeCode(projectId, "modifyTrafficFlow.py");
//        if (null == content) return Result.failure("File system error!");
//        return Result.success(content);
//    }
//
//    // run program by user
//    @PostMapping("/runModifyTrafficFlow")
//    public Result<String> runModifyTrafficFlow(int projectId) {
//        // TODO: set program complete
//        return Result.success(getCodeFromWorkspace(projectId, "modifyTrafficFlow.py"));
//    }
//
//    /** set default vehicle routes algorithm **/
//

//
//    /** set user defined vehicle routes algorithm **/
//
//    // get program
//    @PostMapping("/getUserRoutes")
//    public Result<String> getUserRoutes(int projectId) {
//        String content = getCodeFromWorkspace(projectId, "userRoute.py");
//        if (null == content) return Result.failure("File system error!");
//        return Result.success(content);
//    }
//
//    // save program
//    @PostMapping("/saveUserRoutes")
//    public Result<String> saveUserRoutes(int projectId, String code) {
//        boolean f = saveCodeAtWorkspace(projectId, "userRoute.py", code);
//        if (!f) return Result.failure("File system error!");
//        return Result.success();
//    }
//
//    // initial program by user
//    @PostMapping("/initialUserRoutes")
//    public Result<String> initialUserRoutes(int projectId) {
//        String content = initializeCode(projectId, "userRoute.py");
//        if (null == content) return Result.failure("File system error!");
//        return Result.success(content);
//    }
}
