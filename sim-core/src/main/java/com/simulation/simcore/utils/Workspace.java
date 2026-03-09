package com.simulation.simcore.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class Workspace {
    @Value("${workspace.path}")
    private String workspacePath;
    public static final String[] DATAFILE = {"nodes", "edges", "stations", "devices", "lines", "demands"};


    // -------------------------- User Dir: user_{userId} -------------------------------
    public String getUserDir(int userId) {
        return workspacePath + "user_" + userId;
    }

    public boolean checkUserDir(int userId) {
        String userDir = getUserDir(userId);
        File checkDir = new File(userDir);
        if (!checkDir.exists()) {
            return checkDir.mkdirs();
        }
        return true;
    }

    public void deleteUserDir(int userId) {
        String userDir = getUserDir(userId);
        deleteDirs(new File(userDir));
    }
    // -------------------------- User Dir: user_{userId} -------------------------------



    // ------------------- User Workspace Dir: workspace_{wksId} ------------------------
    public String getWorkspaceDir(int userId, int projectId) {
        return workspacePath + "user_" + userId + "/workspace_" + projectId;
    }

    public boolean checkWorkspaceDir(int userId, int projectId) {
        String workspaceDir = getWorkspaceDir(userId, projectId);
        File checkDir = new File(workspaceDir);
        if (!checkDir.exists()) {
            return !checkDir.mkdirs();
        }
        return false;
    }

    public void deleteWorkspaceDir(int userId, int projectId) {
        String workspaceDir = getWorkspaceDir(userId, projectId);
        deleteDirs(new File(workspaceDir));
    }
    // ------------------- User Workspace Dir: workspace_{wksId} ------------------------


    // ---------- User Data Dir: user_{userId}/workspace_{wks_id}/data ------------------
    public String getDataDir(int userId, int projectId) {
        String workspaceDir = getWorkspaceDir(userId, projectId);
        return workspaceDir + "/data";
    }

    public boolean checkDataDir(int userId, int projectId) {
        String dataDir = getDataDir(userId, projectId);
        File checkDir = new File(dataDir);
        if (!checkDir.exists()) {
            return !checkDir.mkdirs();
        }
        return false;
    }

    public void deleteDataDir(int userId, int projectId) {
        String dataDir = getDataDir(userId, projectId);
        deleteDirs(new File(dataDir));
    }
    // ---------- User Data Dir: user_{userId}/workspace_{wks_id}/data ------------------


    // ---------- User Program Dir: user_{userId}/workspace_{wks_id}/.tmp ---------------
    public String getTmpDir(int userId, int projectId) {
        String workspaceDir = getWorkspaceDir(userId, projectId);
        return workspaceDir + "/.tmp";
    }

    public boolean checkTmpDir(int userId, int projectId) {
        String tmpDir = getTmpDir(userId, projectId);
        File checkDir = new File(tmpDir);
        if (!checkDir.exists()) {
            if (!checkDir.mkdirs()) {
                return !checkDir.mkdirs();
            }
        }
        return false;
    }
    // ---------- User Program Dir: user_{userId}/workspace_{wks_id}/.tmp ---------------


    // ---------- User Result Dir: user_{userId}/workspace_{wks_id}/result ---------------
    public String getResultDir(int userId, int projectId) {
        String workspaceDir = getWorkspaceDir(userId, projectId);
        return workspaceDir + "/result";
    }

    public boolean checkResultDir(int userId, int projectId) {
        String tmpDir = getResultDir(userId, projectId);
        File checkDir = new File(tmpDir);
        if (!checkDir.exists()) {
            if (!checkDir.mkdirs()) {
                return !checkDir.mkdirs();
            }
        }
        return false;
    }
    // ---------- User Result Dir: user_{userId}/workspace_{wks_id}/result ---------------


    /**
     * some tools of file in user's workspace
     **/
    // delete directory
    private static void deleteDirs(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirs(f);
                }
                if (!f.delete()) return;
            }
        }
        file.delete();
    }

    // save data file
    public void saveDataToFile(String path, String data) {
        FileWriter fw;
        try {
            fw = new FileWriter(path);
            fw.write("");
            fw.write(data);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // read JSONObject file from file system
    public JSONObject readJsonFile(String fileName) {
        try {
            BufferedReader br1 = Files.newBufferedReader(Paths.get(fileName));
            JSONObject data = JSONObject.parseObject(br1.readLine());
            br1.close();
            return data;
        } catch (IOException e) {
            return null;
        }
    }



    // ----------------------------- get resource file ----------------------------
    public String getTemplateCode(String programType, String fileName) {
        Resource resource = new ClassPathResource("");
        String resourcePath = null;
        try {
            resourcePath = resource.getFile().getPath() + "/code/" + programType + "/" + fileName;
            byte[] bytes = Files.readAllBytes(Paths.get(resourcePath));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
