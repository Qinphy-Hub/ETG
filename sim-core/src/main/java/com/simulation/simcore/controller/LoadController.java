package com.simulation.simcore.controller;

import com.alibaba.fastjson.JSONObject;
import com.simulation.simcore.entity.Flp;
import com.simulation.simcore.entity.Project;
import com.simulation.simcore.entity.Result;
import com.simulation.simcore.service.FlpService;
import com.simulation.simcore.service.ProjectService;
import com.simulation.simcore.utils.ThreadTool;
import com.simulation.simcore.utils.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/load")
public class LoadController {
    private final Workspace workspace;
    private final ProjectService projectService;
    private final FlpService flpService;
    @Autowired
    public LoadController(Workspace workspace, ProjectService projectService, FlpService flpService) {
        this.workspace = workspace;
        this.projectService = projectService;
        this.flpService = flpService;
    }

    private String getBasePath(int projectId) {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        Project project = projectService.getProject(projectId);
        if (project == null) return null;
        return workspace.getDataDir(userId, projectId);
    }


    // upload any data files
    @PatchMapping("/files")
    public Result<String> uploadFiles(int id, String fileType, @RequestPart("file") MultipartFile file) throws IOException {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        Project project = projectService.getProject(id);
        if (project == null) return Result.failure("Project do not exists!", null);
        if (workspace.checkDataDir(userId, id)) return Result.failure("Data Dir cannot establish!", null);
        String basePath = workspace.getDataDir(userId, project.getId());
        file.transferTo(new File(basePath + "/" + fileType + ".json"));
        return Result.success("Upload user data success!");
    }


    // --------------------------------------------- get traffic network ---------------------------------------------
    // 1. download traffic network data from osm
    @PostMapping("/osm")
    public Result<Map<String, JSONObject>> downloadTrafficNetworkFromOSM(int projectId, String north, String south, String east, String west, String type) throws IOException {
        System.out.println("north: " + north + ", south: " + south + ", east: " + east + ", west: " + west);
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        if (workspace.checkDataDir(userId, projectId)) return Result.failure("File system error!", null);
        Resource resource = new ClassPathResource("download-osm.py");
        String resourcePath = resource.getFile().getPath();
        String[] args = new String[] {"python", resourcePath, "--north", north, "--south", south, "--east", east, "--west", west, "--type", type};
        Map<String, JSONObject> data = new HashMap<>();
        data.put("nodes", null);
        data.put("edges", null);
        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            Process process = builder.start();

            Thread thread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line = null;
                    boolean flag = false;
                    while ((line = reader.readLine()) != null) {
                        if (!flag) {
                            data.put("nodes", JSONObject.parseObject(line));
                            System.out.println("nodes: " + JSONObject.toJSONString(data.get("nodes")));
                            flag = true;
                        } else {
                            data.put("edges", JSONObject.parseObject(line));
                            System.out.println("edges: " + JSONObject.toJSONString(data.get("edges")));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            thread.setDaemon(true);

            StringBuilder errors = new StringBuilder();

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errors.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            errorThread.setDaemon(true);

            thread.start();
            errorThread.start();

            if (!process.waitFor(5, TimeUnit.MINUTES)) {
                process.destroy();

                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        try {
                            Process killprocess = Runtime.getRuntime().exec("taskkill /F /PID " + process.pid());
                            killprocess.waitFor();
                        } catch (Exception e) {
                            process.destroyForcibly();
                        }
                    } else {
                        process.destroyForcibly();
                    }
                }
                return Result.failure("Python runtime error!", data);
            }

            thread.join();
            errorThread.join();

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return Result.failure("Python exit code :" + exitCode + ", error :" + errors, data);
            }
            return Result.success(data);
        } catch (IOException | InterruptedException e) {
            return Result.failure("Python exit!", data);
        }
    }

    // 2. get nodes.json and edges.json from front-side
    @PostMapping("/save-traffic")
    public Result<String> saveTrafficNetworkData(int projectId, String nodes, String edges) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/nodes.json", nodes);
        workspace.saveDataToFile(basePath + "/edges.json", edges);
        return Result.success("Save traffic data successfully!");
    }

    // 3.1 send nodes.json to front-side
    @PostMapping("/nodes")
    public Result<Map<String, JSONObject>> getNodesData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("nodes", workspace.readJsonFile(basePath + "/nodes.json"));
        return Result.success(data);
    }

    // 3.2 send edges.json to front-side
    @PostMapping("/edges")
    public Result<Map<String, JSONObject>> getEdgesData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("edges", workspace.readJsonFile(basePath + "/edges.json"));
        return Result.success(data);
    }

    // 3.3 delete traffic network data
    @DeleteMapping("/delete-traffic/{id}")
    public Result<String> deleteTrafficNetworkData(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File nodesFile = new File(basePath + "/nodes.json");
        File edgesFile = new File(basePath + "/edges.json");
        nodesFile.delete();
        edgesFile.delete();
        return Result.success("Delete traffic data successfully!");
    }
    // --------------------------------------------- get traffic network ---------------------------------------------


    // -------------------------------------------- set station position ---------------------------------------------
    // send station information(stations.json) to front-side
    @PostMapping("/stations")
    public Result<Map<String, JSONObject>> getStationData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("stations", workspace.readJsonFile(basePath + "/stations.json"));
        return Result.success(data);
    }

    // get stations information from front-side
    @PostMapping("/save-station")
    public Result<String> saveStationData(int projectId, String stations) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/stations.json", stations);
        return Result.success("Save stations data successfully!");
    }

    // delete stations information
    @DeleteMapping("/delete-stations/{id}")
    public Result<String> deleteStationsData(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File file = new File(basePath + "/stations.json");
        file.delete();
        return Result.success("Delete stations data successfully!");
    }
    // -------------------------------------------- set station position ---------------------------------------------


    // ------------------------------------------ set power grid topology --------------------------------------------
    // send power grid information to front-side
    @PostMapping("/devices")
    public Result<Map<String, JSONObject>> getDeviceData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("devices", workspace.readJsonFile(basePath + "/devices.json"));
        data.put("lines", workspace.readJsonFile(basePath + "/lines.json"));
        return Result.success(data);
    }

    // get power grid information from front-side
    @PostMapping("/save-device")
    public Result<String> saveDevicesData(int projectId, String devices, String lines) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/devices.json", devices);
        workspace.saveDataToFile(basePath + "/lines.json", lines);
        return Result.success("Save power grid data successfully!");
    }

    // delete power grid information
    @DeleteMapping("/delete-device/{id}")
    public Result<String> deleteDevicesData(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File file1 = new File(basePath + "/devices.json");
        file1.delete();
        File file2 = new File(basePath + "/lines.json");
        file2.delete();
        return Result.success("Delete power grid data successfully!");
    }
    // ------------------------------------------ set power grid topology --------------------------------------------


    // -------------------------------------------- set traffic flow data --------------------------------------------
    // send traffic flow data to front-side
    @PostMapping("/traffic-flow")
    public Result<Map<String, JSONObject>> getTrafficFlowData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("trafficFlow", workspace.readJsonFile(basePath + "/traffic-flow.json"));
        return Result.success(data);
    }

    // get traffic flow data
    @PostMapping("/save-traffic-flow")
    public Result<String> saveTrafficFlowData(int projectId, String trafficFlow) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/traffic-flow.json", trafficFlow);
        return Result.success("Save traffic flow data successfully!");
    }

    // delete traffic flow data
    @DeleteMapping("/delete-traffic-flow/{id}")
    public Result<String> deleteTrafficFlowData(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File file = new File(basePath + "/traffic-flow.json");
        file.delete();
        return Result.success("Delete potential site data successfully!");
    }
    // -------------------------------------------- set traffic flow data --------------------------------------------


    // -------------------------------------- set potential site data of stations ------------------------------------
    // send potential site data to front-side
    @PostMapping("/sites")
    public Result<Map<String, JSONObject>> getSiteData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("sites", workspace.readJsonFile(basePath + "/sites.json"));
        return Result.success(data);
    }

    // get potential site data
    @PostMapping("/save-site")
    public Result<String> saveSitesData(int projectId, String sites) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/sites.json", sites);
        return Result.success("Save sites data successfully!");
    }

    // delete potential site data
    @DeleteMapping("/delete-site/{id}")
    public Result<String> deleteSitesData(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File file = new File(basePath + "/sites.json");
        file.delete();
        return Result.success("Delete potential site data successfully!");
    }
    // -------------------------------------- set potential site data of stations ------------------------------------


    // ---------------------------------------------- set point demands ----------------------------------------------
    // send pont demand data to front-side
    @PostMapping("/point-demands")
    public Result<Map<String, JSONObject>> getPointDemandData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("pointDemands", workspace.readJsonFile(basePath + "/point-demands.json"));
        return Result.success(data);
    }

    // get point demand data
    @PostMapping("/save-pointDemand")
    public Result<String> savePointDemandsData(int projectId, String pointDemands) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/point-demands.json", pointDemands);
        return Result.success("Save demand points data successfully!");
    }

    // delete point demand data
    @DeleteMapping("/delete-pointDemand/{id}")
    public Result<String> deletePointDemandsData(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File file = new File(basePath + "/point-demands.json");
        file.delete();
        return Result.success("Delete demand points data successfully!");
    }
    // ---------------------------------------------- set point demands ----------------------------------------------


    // ------------------------------------------------ set od demands -----------------------------------------------
    // send od pairs data to front-side
    @PostMapping("/od-pairs")
    public Result<Map<String, JSONObject>> getOdPairData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("odPairs", workspace.readJsonFile(basePath + "/od-pairs.json"));
        return Result.success(data);
    }

    // get od pairs demands data
    @PostMapping("/save-odDemand")
    public Result<String> saveODDemandsData(int projectId, String odDemands) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/od-pairs.json", odDemands);
        return Result.success("Save od pairs data successfully!");
    }

    // delete od pairs data
    @DeleteMapping("/delete-odDemand/{id}")
    public Result<String> deleteODDemandsData(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File file = new File(basePath + "/od-pairs.json");
        file.delete();
        return Result.success("Delete od pairs data successfully!");
    }
    // ------------------------------------------------ set od demands -----------------------------------------------


    // -------------------------------------------- set station position ---------------------------------------------
    // send real demands information(read-demands.json) to front-side
    @PostMapping("/real-demands")
    public Result<Map<String, JSONObject>> getRealDemandsData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("realDemands", workspace.readJsonFile(basePath + "/point-demands.json"));
        return Result.success(data);
    }

    // get real demands information from front-side
    @PostMapping("/save-real-demands")
    public Result<String> saveRealDemandsData(int projectId, String demands) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/point-demands.json", demands);
        return Result.success("Save stations data successfully!");
    }

    // delete real demands information
    @DeleteMapping("/delete-real-demands/{id}")
    public Result<String> deleteRealDemandsData(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File file = new File(basePath + "/point-demands.json");
        file.delete();
        return Result.success("Delete stations data successfully!");
    }
    // -------------------------------------------- set station position ---------------------------------------------


    // --------------------------------------------- get json from result --------------------------------------------
    // 1. get flp result
    @PostMapping("/res/flp")
    public Result<Map<Integer, JSONObject>> getFlpData(int projectId) {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        List<Flp> flpList = flpService.getFlpList(projectId);
        Map<Integer, JSONObject> data = new HashMap<>();
        for (Flp flp : flpList) {
            String basePath = workspace.getResultDir(userId, projectId);
            data.put(flp.getId(), workspace.readJsonFile(basePath + "/stations_" + flp.getId() + ".json"));
        }
        return Result.success(data);
    }

    // 2. get routes result
    @PostMapping("/res/route")
    public Result<Map<String, JSONObject>> getPositionData(int projectId) {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        Map<String, JSONObject> data = new HashMap<>();
        String basePath = workspace.getResultDir(userId, projectId);
        data.put("positions", workspace.readJsonFile(basePath + "/positions.json"));
        data.put("station_power", workspace.readJsonFile(basePath + "/stations_power.json"));
        return Result.success(data);
    }









    /** get data from file system **/



    @PostMapping("/power-flow")
    public Result<Map<String, JSONObject>> getPowerFlowData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("powerFlow", workspace.readJsonFile(basePath + "/power-flow.json"));
        return Result.success(data);
    }

    @PostMapping("/start-end")
    public Result<Map<String, JSONObject>> getStartEnd(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("start-end", workspace.readJsonFile(basePath + "/start-end.json"));
        return Result.success(data);
    }

    @PostMapping("/all-data")
    public Result<Map<String, JSONObject>> getAllData(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("nodes", workspace.readJsonFile(basePath + "/nodes.json"));
        data.put("edges", workspace.readJsonFile(basePath + "/edges.json"));
        data.put("stations", workspace.readJsonFile(basePath + "/stations.json"));
        data.put("pointDemands", workspace.readJsonFile(basePath + "/point-demands.json"));
        data.put("odPairs", workspace.readJsonFile(basePath + "/od-pairs.json"));
        data.put("sites", workspace.readJsonFile(basePath + "/sites.json"));
        data.put("devices", workspace.readJsonFile(basePath + "/devices.json"));
        data.put("lines", workspace.readJsonFile(basePath + "/lines.json"));
        data.put("trafficFlow", workspace.readJsonFile(basePath + "/traffic-flow.json"));
        data.put("powerFlow", workspace.readJsonFile(basePath + "/power-flow.json"));
        data.put("stationInfo", workspace.readJsonFile(basePath + "/station-info.json"));
        data.put("start-end", workspace.readJsonFile(basePath + "/start-end.json"));
        return Result.success(data);
    }


    /** save data from front-side **/







    @PostMapping("/save-start-end")
    public Result<String> saveStartEnd(int projectId, String startEnd) {
        String basePath = getBasePath(projectId);
        workspace.saveDataToFile(basePath + "/start-end.json", startEnd);
        return Result.success("Save start end data successfully!");
    }


    /** delete data include file system **/






    @DeleteMapping("/delete-start-end/{id}")
    public Result<String> deleteStartEnd(@PathVariable int id) {
        String basePath = getBasePath(id);
        if (basePath == null) return Result.failure("Project do not exists!");
        File file = new File(basePath + "/start-end.json");
        file.delete();
        return Result.success("Delete start end data successfully!");
    }





    /** simulation result **/

    // get result
    @PostMapping("/sim-result")
    public Result<Map<String, JSONObject>> simResult(int projectId) {
        String basePath = getBasePath(projectId);
        if (basePath == null) return Result.failure("Project do not exists!", null);
        Map<String, JSONObject> data = new HashMap<>();
        data.put("sim", workspace.readJsonFile(basePath + "/ret.json"));
        return Result.success(data);
    }
}
