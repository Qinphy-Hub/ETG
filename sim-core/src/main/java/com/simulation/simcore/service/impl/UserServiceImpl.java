package com.simulation.simcore.service.impl;

import com.simulation.simcore.entity.User;
import com.simulation.simcore.mapper.UserMapper;
import com.simulation.simcore.service.UserService;
import com.simulation.simcore.utils.Md5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private UserMapper userMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public int registerUser(String username, String password, String email) {
        String md5Password = Md5.getMD5String(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(md5Password);
        user.setEmail(email);
        userMapper.insertUser(user);
        return user.getId();
    }

    @Override
    public void deleteUser(int id) {
        userMapper.deleteUser(id);
    }

    @Override
    public void updateUsername(int id, String username) {
        userMapper.updateUsername(id, username);
    }

    @Override
    public void updatePassword(int id, String password) {
        String md5Password = Md5.getMD5String(password);
        userMapper.updatePassword(id, md5Password);
    }

    @Override
    public void uploadUserAvatar(int id, String avatar) {
        userMapper.updateAvatar(id, avatar);
    }

    @Override
    public User getUserInfo(int id) {
        return userMapper.getUserById(id);
    }

    @Override
    public User findUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    @Override
    public List<User> findAllUsers() {
        return userMapper.getAllUsers();
    }
}
