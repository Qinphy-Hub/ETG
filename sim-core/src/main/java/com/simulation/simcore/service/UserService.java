package com.simulation.simcore.service;

import com.simulation.simcore.entity.User;

import java.util.List;

public interface UserService {
    // add a user which is not exists
    int registerUser(String username, String password, String email);

    // admin have authority to delete normal user
    void deleteUser(int id);

    // user can change his/her username
    void updateUsername(int id, String username);
    // user can change his/her password
    void updatePassword(int id, String password);
    // user can update his/her avatar
    void uploadUserAvatar(int id, String avatar);

    // get user info to show on html page
    User getUserInfo(int id);
    // email is unique, to find user exists
    User findUserByEmail(String email);
    // admin manage all users
    List<User> findAllUsers();
}
