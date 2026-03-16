package com.studyhelper.service;

import com.studyhelper.dto.LoginRequest;
import com.studyhelper.dto.RegisterRequest;
import com.studyhelper.dto.UserDTO;
import com.studyhelper.entity.User;
import com.studyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty() && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        if (request.getStudentId() != null && !request.getStudentId().isEmpty() && userRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("学号已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // 使用 BCrypt 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStudentId(request.getStudentId());
        user.setMajor(request.getMajor());
        user.setGrade(request.getGrade());
        user.setRole(User.Role.STUDENT);

        User savedUser = userRepository.save(user);
        return UserDTO.fromUser(savedUser);
    }

    public UserDTO login(LoginRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();

        Optional<User> userOptional = userRepository.findByUsername(account);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(account);
        }
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByStudentId(account);
        }
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmployeeId(account);
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOptional.get();
        // 使用 BCrypt 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        if (user.getRole() == User.Role.TEACHER && !Boolean.TRUE.equals(user.getIsVerified())) {
            throw new RuntimeException("教师身份尚未验证，请联系管理员");
        }

        return UserDTO.fromUser(user);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserDTO.fromUser(user);
    }

    public UserDTO updateUser(Long id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getMajor() != null) {
            user.setMajor(request.getMajor());
        }
        if (request.getGrade() != null) {
            user.setGrade(request.getGrade());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty() && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return UserDTO.fromUser(updatedUser);
    }

    public void updateAvatar(Long id, String avatarUrl) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatar(avatarUrl);
        userRepository.save(user);
    }
    
    // 新增方法供AuthController使用
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public User findByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId).orElse(null);
    }
    
    public User saveUser(User user) {
        // 如果是新用户且密码未加密，则进行加密
        if (user.getId() == null && user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }
    
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
