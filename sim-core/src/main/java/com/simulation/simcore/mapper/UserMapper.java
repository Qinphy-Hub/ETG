package com.simulation.simcore.mapper;

import com.simulation.simcore.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO user(username, password, email) VALUES (#{username},#{password}, #{email})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void insertUser(User user);

    @Delete("DELETE FROM user WHERE id=#{id}")
    void deleteUser(int id);

    @Update("UPDATE user SET username=#{username} WHERE id=#{id}")
    void updateUsername(int id, String username);

    @Update("UPDATE user SET password=#{password} WHERE id=#{id}")
    void updatePassword(int id, String password);

    @Update("UPDATE user SET user_pic=#{avatar} WHERE id=#{id}")
    void updateAvatar(int id, String avatar);

    @Select("SELECT * FROM user WHERE id=#{id}")
    User getUserById(int id);

    @Select("SELECT * FROM user WHERE email=#{email}")
    User getUserByEmail(String email);

    @Select("SELECT * FROM user")
    List<User> getAllUsers();
}
