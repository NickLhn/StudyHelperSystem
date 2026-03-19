package com.studyhelper.config;

import com.studyhelper.entity.User;
import com.studyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${admin.init.enabled:true}")
    private boolean adminInitEnabled;

    @Value("${admin.init.username:admin}")
    private String adminUsername;

    @Value("${admin.init.email:admin@example.com}")
    private String adminEmail;

    @Value("${admin.init.password:ChangeMe123!}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (!adminInitEnabled) {
            System.out.println("默认管理员初始化已关闭");
            return;
        }

        // 检查是否已存在管理员用户
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword)); // 使用BCrypt加密
            admin.setRole(User.Role.ADMIN);
            
            userRepository.save(admin);
            System.out.println("默认管理员账号已创建:");
            System.out.println("用户名: " + adminUsername);
            System.out.println("密码: " + adminPassword);
            System.out.println("请在部署前修改 admin.init.password 或更新该账户密码");
        } else {
            System.out.println("管理员账号已存在");
        }
    }
}
