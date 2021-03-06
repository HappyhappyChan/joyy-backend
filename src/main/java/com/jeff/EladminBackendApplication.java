package com.jeff;

import com.jeff.config.ElPermissionConfig;
import com.jeff.utils.SpringContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;

@SpringBootApplication
public class EladminBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(EladminBackendApplication.class, args);
        System.out.println("http://localhost:8080");
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }
}
