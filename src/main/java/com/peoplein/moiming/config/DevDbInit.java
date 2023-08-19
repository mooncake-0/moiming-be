package com.peoplein.moiming.config;

import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DevDbInit {

    @Bean
    @Profile("dev")
    CommandLineRunner init(RoleRepository roleRepository) {
        return (args) -> {
            Role roleUser = new Role(1L, "유저 권한", RoleType.USER);
            Role roleAdmin = new Role(2L, "관리자 권한", RoleType.ADMIN);

            roleRepository.save(roleUser);
            roleRepository.save(roleAdmin);
        };
    }
}
