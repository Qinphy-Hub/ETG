package com.simulation.simcore.controller;

import com.simulation.simcore.entity.Result;
import com.simulation.simcore.entity.User;
import com.simulation.simcore.service.EmailService;
import com.simulation.simcore.service.UserService;
import com.simulation.simcore.utils.Jwt;
import com.simulation.simcore.utils.Md5;
import com.simulation.simcore.utils.ThreadTool;
import com.simulation.simcore.utils.Workspace;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Value("${software.name}")
    private String softwareName;

    private final Workspace workspace;
    private final EmailService emailService;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;
    @Autowired
    public UserController(EmailService emailService, UserService userService, StringRedisTemplate redisTemplate, Workspace workspace) {
        this.emailService = emailService;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.workspace = workspace;
    }

    // private util variable
    private static final String ALPHABETS_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHABETS_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";

    // generate 6 char verification code
    private String getVerificationCode() {
        String allCharacters = ALPHABETS_UPPER + ALPHABETS_LOWER + DIGITS;
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = (int)(Math.random() * allCharacters.length());
            randomString.append(allCharacters.charAt(randomIndex));
        }
        return randomString.toString();
    }

    // generate 12 char random password
    private String getRandomPassword() {
        String allLetters = ALPHABETS_UPPER + ALPHABETS_LOWER;
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int randomLetter = (int)(Math.random() * allLetters.length());
            randomString.append(allLetters.charAt(randomLetter));
        }
        for (int i = 0; i < 5; i++) {
            int randomDigit = (int)(Math.random() * DIGITS.length());
            int randomIndex = (int)(Math.random() * 12);
            randomString.setCharAt(randomIndex, DIGITS.charAt(randomDigit));
        }
        return randomString.toString();
    }

    // get verification code
    @PostMapping("/verification-code")
    public Result<String> getVerificationCode(
            @Pattern(regexp = "^[A-Za-z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "Email format error!") String email)
    {
        // check email exists
        User user = userService.findUserByEmail(email);
        if (user != null) return Result.failure("This email address is registered.");
        // send verification code
        String title = softwareName;
        String code = getVerificationCode();
        String content = "Your Verification Code is: " + code;
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(email, code, 5, TimeUnit.MINUTES);
        if (emailService.sendEmail(email, title, content)) {
            return Result.success("Verification Code is valid in five minutes.");
        }
        return Result.failure("Email server error!");
    }

    @PostMapping("/forget-password")
    public Result<String> forgetPassword(
            @Pattern(regexp = "^[A-Za-z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "Email format error!") String email)
    {
        // check email exists
        User user = userService.findUserByEmail(email);
        if (user == null) return Result.failure("This email address is not registered.");
        // set a new password to user
        String newPassword = getRandomPassword();
        userService.updatePassword(user.getId(), newPassword);
        String title = softwareName;
        String content = "Your New Password is " + newPassword;
        if (emailService.sendEmail(email, title, content)) {
            return Result.success("Your New Password has been send to your email address.");
        }
        return Result.failure("Email server error!");
    }

    @PostMapping("/register")
    public Result<String> register(
            @Pattern(regexp = "^.{5,20}", message = "username must contained 5 to 20 characters.") String username,
            @Pattern(regexp = "^(?=.*[a-zA-Z0-9].*)(?=.*[a-zA-Z.!@#$%^&*].*)(?=.*[0-9.!@#$%^&*].*).{6,32}$", message = "password must contains 6 to 32 characters, and at least two types of digits, letters and special symbols.") String password,
            @Pattern(regexp = "^[A-Za-z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "email format error!") String email,
            @Pattern(regexp = "^.{6}", message = "verify code must contained 6 characters!") String code)
    {
        // check verify code
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String realCode = operations.get(email);
        if (!code.equals(realCode)) return Result.failure("Verification Code is not correct.");
        // check user
        User user = userService.findUserByEmail(email);
        if (user != null) return Result.failure("This email address has been registered.");
        int id = userService.registerUser(username, password, email);
        // create user dir
        workspace.checkUserDir(id);
        return Result.success("Thanks to join us.");
    }

    @PostMapping("/login")
    public Result<String> login(
            @Pattern(regexp = "^[A-Za-z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "email format error!") String email,
            @Pattern(regexp = "^(?=.*[a-zA-Z0-9].*)(?=.*[a-zA-Z.!@#$%^&*].*)(?=.*[0-9.!@#$%^&*].*).{6,32}$", message = "password must contains 6 to 32 characters, and at least two types of digits, letters and special symbols.") String password)
    {
        // check user
        User user = userService.findUserByEmail(email);
        if (user == null) return Result.failure("This email address is not registered.");
        if (Md5.getMD5String(password).equals(user.getPassword())) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            claims.put("username", user.getUsername());
            String token = Jwt.genToken(claims);
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(token, token, 8, TimeUnit.HOURS);
            return Result.success(token);
        }
        return Result.failure("The password is incorrect.");
    }

    @GetMapping("/info")
    public Result<User> getInfo() {
        Map<String, Object> claims = ThreadTool.get();
        int id = (int) claims.get("id");
        User user = userService.getUserInfo(id);
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<List<User>> getList() {
        Map<String, Object> claims = ThreadTool.get();
        int id = (int) claims.get("id");
        if (id != 1) return Result.failure("Sorry! You do not have authority.", null);
        List<User> users = userService.findAllUsers();
        return Result.success(users);
    }

    @PatchMapping("/modify-username")
    public Result<String> modifyUsername(
            @RequestParam @Pattern(regexp = "^.{5,20}", message = "username must contained 5 to 20 characters.") String username)
    {
        Map<String, Object> claims = ThreadTool.get();
        int id = (int) claims.get("id");
        userService.updateUsername(id, username);
        return Result.success("Modify username successfully.");
    }

    @PatchMapping("/modify-password")
    public Result<String> modifyPassword(@RequestBody Map<String, String> params, @RequestHeader("Authorization") String token) {
        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");
        String repPwd = params.get("repPassword");
        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(repPwd)) {
            return Result.failure("Lack of password data.");
        }
        if (!newPwd.equals(repPwd)) return Result.failure("The new and repeat passwords are different.");
        // update token and database
        Map<String, Object> claims = ThreadTool.get();
        int id = (int) claims.get("id");
        User user = userService.getUserInfo(id);
        if (!user.getPassword().equals(Md5.getMD5String(oldPwd))) {
            return Result.failure("old password error!");
        }
        userService.updatePassword(id, newPwd);
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success("Modify password successfully.");
    }

    @PatchMapping("/upload-avatar")
    public Result<String> uploadAvatar(MultipartFile file) throws IOException {
        Map<String, Object> claims = ThreadTool.get();
        int id = (int) claims.get("id");
        if (!workspace.checkUserDir(id)) return Result.failure("Cannot create user directory!");
        String absolutePath = workspace.getUserDir(id) + "/avatar.png";
        String relativePath = "/user_" + id + "/avatar.png";
        file.transferTo(new File(absolutePath));
        userService.uploadUserAvatar(id, relativePath);
        return Result.success(relativePath);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable int id, @RequestHeader("Authorization") String token) {
        User user = userService.getUserInfo(id);
        if (user == null) return Result.failure("User not found.");
        userService.deleteUser(id);
        // account cancellation function
        Map<String, Object> claims = ThreadTool.get();
        int userId = (int) claims.get("id");
        if (userId == id) {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.getOperations().delete(token);
        }
        // delete user workspace
        workspace.deleteUserDir(userId);
        return Result.success("Delete user successfully.");
    }

    @GetMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String token) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success("Logout successfully.");
    }
}
