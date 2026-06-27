package com.livraison.backend.config;

import com.livraison.backend.entity.Role;
import com.livraison.backend.entity.User;
import com.livraison.backend.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {

        return args -> {


            String adminEmail = "admin@wasali.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("Admin123!"));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setEmailVerified(true);
                admin.setEnabled(true);

                userRepository.save(admin);

                System.out.println("=======================================");
                System.out.println("✅ ADMIN CREATED SUCCESSFULLY");
                System.out.println("📧 Email: " + adminEmail);
                System.out.println("🔑 Password: Admin123!");
                System.out.println("=======================================");

            } else {
                System.out.println("ℹ️ Admin already exists");
            }
        };
    }
}