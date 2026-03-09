package com.simulation.simcore.controller;

import com.alibaba.fastjson.JSONObject;
import com.simulation.simcore.entity.*;
import com.simulation.simcore.service.*;
import com.simulation.simcore.utils.ThreadTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grid")
public class GridController {
    private final UserService userService;
    private final AclinesService aclinesService;
    private final DclinesService dclinesService;
    private final Transformers3Service transformers3Service;
    private final TransformersService transformersService;

    @Autowired
    public GridController(UserService userService, AclinesService aclinesService,
                          DclinesService dclinesService, TransformersService transformersService,
                          Transformers3Service transformers3Service) {
        this.userService = userService;
        this.aclinesService = aclinesService;
        this.dclinesService = dclinesService;
        this.transformers3Service = transformers3Service;
        this.transformersService = transformersService;
    }

    private boolean userAuthorization() {
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        User user = userService.getUserInfo(userId);
        return user == null;
    }

    // AC lines
    @PostMapping("/aclines")
    public Result<List<Aclines>> getAclinesList() {
        if (userAuthorization()) return Result.failure("user not found!", null);
        List<Aclines> aclineList = aclinesService.getAclinesList();
        if (aclineList == null) return Result.failure("AC lines database not found!", null);
        return Result.success(aclineList);
    }

    @PostMapping("/acline/{id}")
    public Result<Aclines> getAcline(@PathVariable int id) {
        if (userAuthorization()) return Result.failure("user not found!", null);
        Aclines acline = aclinesService.getAclinesById(id);
        if (acline == null) return Result.failure("AC line not found!", null);
        return Result.success(acline);
    }

    // DC lines
    @PostMapping("/dclines")
    public Result<List<Dclines>> getDclinesList() {
        if (userAuthorization()) return Result.failure("user not found!", null);
        List<Dclines> dclineList = dclinesService.getDclinesList();
        if (dclineList == null) return Result.failure("DC lines database not found!", null);
        return Result.success(dclineList);
    }

    @PostMapping("/dcline/{id}")
    public Result<Dclines> getDcline(@PathVariable int id) {
        if (userAuthorization()) return Result.failure("user not found!", null);
        Dclines dcline = dclinesService.getDclinesById(id);
        if (dcline == null) return Result.failure("DC line not found!", null);
        return Result.success(dcline);
    }

    // Transformer
    @PostMapping("/transformers")
    public Result<List<Transformers>> getTransformersList() {
        if (userAuthorization()) return Result.failure("user not found!", null);
        List<Transformers> transformersList = transformersService.getTransformersList();
        if (transformersList == null) return Result.failure("transformers database not found!", null);
        return Result.success(transformersList);
    }

    @PostMapping("/transformer/{id}")
    public Result<Transformers> getTransformer(@PathVariable int id) {
        if (userAuthorization()) return Result.failure("user not found!", null);
        Transformers transformer = transformersService.getTransformersById(id);
        if (transformer == null) return Result.failure("transformer not found!", null);
        return Result.success(transformer);
    }

    // Transformers3
    @PostMapping("/transformers3")
    public Result<List<Transformers3>> getTransformers3List() {
        if (userAuthorization()) return Result.failure("user not found!", null);
        List<Transformers3> transformers3List = transformers3Service.getTransformers3List();
        if (transformers3List == null) return Result.failure("transformers3 database not found!", null);
        return Result.success(transformers3List);
    }

    @PostMapping("/transformer3/{id}")
    public Result<Transformers3> getTransformers3(@PathVariable int id) {
        if (userAuthorization()) return Result.failure("user not found!", null);
        Transformers3 transformers3 = transformers3Service.getTransformers3ById(id);
        if (transformers3 == null) return Result.failure("transformers3 database not found!", null);
        return Result.success(transformers3);
    }
}
