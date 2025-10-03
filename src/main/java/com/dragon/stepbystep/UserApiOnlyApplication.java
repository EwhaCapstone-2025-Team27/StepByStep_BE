package com.dragon.stepbystep;

import com.dragon.stepbystep.common.ApiResponse; // 공통 유틸이 다른 패키지면 빼도 됨
import com.dragon.stepbystep.controller.AuthController;
import com.dragon.stepbystep.controller.UserController;
import com.dragon.stepbystep.domain.BaseTimeEntity;
import com.dragon.stepbystep.domain.TokenBlacklist;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.repository.UserRepository;
import com.dragon.stepbystep.config.SecurityConfig;
import com.dragon.stepbystep.service.TokenBlacklistService;
import com.dragon.stepbystep.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // 필요 시 제외/포함 선택
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(/* exclude 제거 권장 */)
@ComponentScan(basePackageClasses = {
        AuthController.class, UserController.class,
        UserService.class, TokenBlacklistService.class,
        SecurityConfig.class,
        com.dragon.stepbystep.security.JwtAuthenticationFilter.class // ← 반드시 포함
})
@EntityScan(basePackageClasses = { User.class, TokenBlacklist.class, BaseTimeEntity.class })
@EnableJpaRepositories(basePackageClasses = { UserRepository.class })
@EnableJpaAuditing
public class UserApiOnlyApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApiOnlyApplication.class, args);
    }
}
