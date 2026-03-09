package com.simulation.simcore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class User {
    private int id;
    private String username;
    @JsonIgnore // Ignore password data when transmit User object.
    private String password;
    private String email;
    private String userPic;
}
