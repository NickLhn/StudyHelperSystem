package com.studyhelper.config;

import com.studyhelper.entity.User;
import com.studyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员用户
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin")); // 使用BCrypt加密
            admin.setRole(User.Role.ADMIN);
            
            userRepository.save(admin);
            System.out.println("默认管理员账号已创建:");
            System.out.println("用户名: admin");
            System.out.println("密码: admin");
        } else {
            System.out.println("管理员账号已存在");
        }
    }
}