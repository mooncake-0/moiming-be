package com.peoplein.moiming.config;

import com.peoplein.moiming.security.token.logout.LogoutTokenDb;
import com.peoplein.moiming.security.token.logout.LogoutTokenManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfiguration {


    /*
     앱 전체적으로 사용되는 Bean 관리
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LogoutTokenManager logoutTokenManager() {
        return new LogoutTokenDb();
    }
}
