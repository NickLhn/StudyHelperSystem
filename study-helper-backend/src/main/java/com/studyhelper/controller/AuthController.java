package com.studyhelper.controller;

import com.studyhelper.entity.User;
import com.studyhelper.entity.InvitationCode;
import com.studyhelper.repository.InvitationCodeRepository;
import com.studyhelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private InvitationCodeRepository invitationCodeRepository;

    /**
     * 教师注册
     */
    @PostMapping("/teacher/register")
    public ResponseEntity<Map<String, Object>> teacherRegister(@RequestBody TeacherRegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            InvitationCode invitationCode = null;
            if (request.getInvitationCode() != null && !request.getInvitationCode().isEmpty()) {
                invitationCode = invitationCodeRepository.findByCode(request.getInvitationCode())
                        .orElse(null);
                if (invitationCode == null) {
                    response.put("success", false);
                    response.put("message", "邀请码无效");
                    return ResponseEntity.badRequest().body(response);
                }
                if (Boolean.TRUE.equals(invitationCode.getIsUsed())) {
                    response.put("success", false);
                    response.put("message", "邀请码已被使用");
                    return ResponseEntity.badRequest().body(response);
                }
                if (invitationCode.getExpiresAt() != null && invitationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                    response.put("success", false);
                    response.put("message", "邀请码已过期");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                if (!validateTeacherVerification(request.getEmployeeId(), request.getVerificationCode())) {
                    response.put("success", false);
                    response.put("message", "教师资格验证失败");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // 检查用户名是否已存在
            if (userService.findByUsername(request.getUsername()) != null) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查邮箱是否已存在
            if (request.getEmail() != null && !request.getEmail().isEmpty() && userService.findByEmail(request.getEmail()) != null) {
                response.put("success", false);
                response.put("message", "邮箱已被注册");
                return ResponseEntity.badRequest().body(response);
            }

            // 创建教师用户
            User teacher = new User();
            teacher.setUsername(request.getUsername());
            teacher.setEmail(request.getEmail());
            teacher.setPassword(request.getPassword()); // Service层会进行加密
            teacher.setNickname(request.getNickname());
            teacher.setRole(User.Role.TEACHER);
            teacher.setDepartment(request.getDepartment());
            teacher.setTeacherTitle(request.getTeacherTitle());
            teacher.setEmployeeId(request.getEmployeeId());
            teacher.setIsVerified(true);
            teacher.setVerificationCode(request.getVerificationCode());

            User savedUser = userService.saveUser(teacher);
            
            if (invitationCode != null) {
                invitationCode.setIsUsed(true);
                invitationCode.setUsedByUserId(savedUser.getId());
                invitationCode.setUsedAt(LocalDateTime.now());
                invitationCodeRepository.save(invitationCode);
            }
            
            response.put("success", true);
            response.put("message", "教师注册成功");
            response.put("userId", savedUser.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 教师登录
     */
    @PostMapping("/teacher/login")
    public ResponseEntity<Map<String, Object>> teacherLogin(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.findByUsername(request.getAccount());
            if (user == null) {
                user = userService.findByEmail(request.getAccount());
            }
            if (user == null) {
                user = userService.findByEmployeeId(request.getAccount());
            }

            if (user == null || !userService.checkPassword(request.getPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证是否为教师角色
            if (user.getRole() != User.Role.TEACHER) {
                response.put("success", false);
                response.put("message", "该账号不是教师账号");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证教师身份是否已确认
            if (!user.getIsVerified()) {
                response.put("success", false);
                response.put("message", "教师身份尚未验证，请联系管理员");
                return ResponseEntity.badRequest().body(response);
            }

            // 生成模拟token（实际项目中应该使用JWT）
            String token = "teacher_token_" + user.getId() + "_" + System.currentTimeMillis();
            
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取教师验证码（模拟接口）
     */
    @PostMapping("/teacher/get-verification-code")
    public ResponseEntity<Map<String, Object>> getTeacherVerificationCode(@RequestBody VerificationCodeRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 模拟验证教职工编号是否存在（实际应连接学校人事系统）
            if (!isValidEmployeeId(request.getEmployeeId())) {
                response.put("success", false);
                response.put("message", "教职工编号不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 生成6位数字验证码
            String verificationCode = String.format("%06d", new Random().nextInt(999999));
            
            // 实际项目中应该发送邮件或短信，这里只是返回给前端
            response.put("success", true);
            response.put("message", "验证码已生成");
            response.put("verificationCode", verificationCode); // 开发阶段直接返回，生产环境应通过邮件/短信发送
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取验证码失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 验证教师资格（模拟验证逻辑）
     */
    private boolean validateTeacherVerification(String employeeId, String verificationCode) {
        // 实际项目中应该连接学校的教师管理系统进行验证
        // 这里使用简单的验证逻辑作为示例
        
        // 模拟验证：教职工编号以"T"开头，验证码为6位数字
        if (employeeId != null && employeeId.startsWith("T") && 
            verificationCode != null && verificationCode.matches("\\d{6}")) {
            return true;
        }
        
        // 开发阶段可以接受特定的测试数据
        if ("T001".equals(employeeId) && "123456".equals(verificationCode)) {
            return true;
        }
        
        return false;
    }

    /**
     * 验证教职工编号是否有效（模拟）
     */
    private boolean isValidEmployeeId(String employeeId) {
        // 实际应该查询人事系统
        // 这里简单验证格式
        return employeeId != null && (employeeId.startsWith("T") || employeeId.matches("T\\d+"));
    }

    /**
     * 创建用户响应对象
     */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("nickname", user.getNickname());
        userMap.put("role", user.getRole().toString());
        userMap.put("department", user.getDepartment());
        userMap.put("teacherTitle", user.getTeacherTitle());
        userMap.put("employeeId", user.getEmployeeId());
        return userMap;
    }

    // 请求数据传输对象
    public static class TeacherRegisterRequest {
        private String username;
        private String email;
        private String password;
        private String nickname;
        private String invitationCode;
        private String department;
        private String teacherTitle;
        private String employeeId;
        private String verificationCode;

        // getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        
        public String getInvitationCode() { return invitationCode; }
        public void setInvitationCode(String invitationCode) { this.invitationCode = invitationCode; }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public String getTeacherTitle() { return teacherTitle; }
        public void setTeacherTitle(String teacherTitle) { this.teacherTitle = teacherTitle; }
        
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        
        public String getVerificationCode() { return verificationCode; }
        public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    }

    public static class LoginRequest {
        private String account;
        private String password;

        public String getAccount() { return account; }
        public void setAccount(String account) { this.account = account; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class VerificationCodeRequest {
        private String employeeId;

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    }
}
