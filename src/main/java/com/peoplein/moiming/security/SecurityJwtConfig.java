package com.peoplein.moiming.security;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.security.filter.JwtAuthenticationFilter;
import com.peoplein.moiming.security.filter.MoimingLoginFilter;
import com.peoplein.moiming.security.handler.MoimingLoginFailureHandler;
import com.peoplein.moiming.security.handler.MoimingLoginSuccessHandler;
import com.peoplein.moiming.security.auth.JwtAuthenticationProvider;
import com.peoplein.moiming.security.token.JwtTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.service.SecurityMemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import javax.servlet.Filter;
import java.util.List;


@Configuration
public class SecurityJwtConfig {

    /*
     Security 설정을 위한 Bean 설정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new SecurityMemberService();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new JwtAuthenticationProvider(userDetailsService(), passwordEncoder());
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return new MoimingLoginSuccessHandler(moimingTokenProvider(), (SecurityMemberService) userDetailsService());
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return new MoimingLoginFailureHandler();
    }

    @Bean
    public MoimingTokenProvider moimingTokenProvider() {
        return new JwtTokenProvider();
    }


//    @Bean
//    public JwtAuthenticationFilter jwtAuthenticationFilter() {
//
//        return new JwtAuthenticationFilter(userDetailsService(), moimingTokenProvider());
//    }

    public class MoimingSecurityFilterManager extends AbstractHttpConfigurer<MoimingSecurityFilterManager, HttpSecurity> {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
            http.addFilterAfter(jwtAuthenticationFilter(authenticationManager), LogoutFilter.class);
            http.addFilterAfter(moimingLoginFilter(authenticationManager), LogoutFilter.class);
            super.configure(http);
        }
    }

    public MoimingLoginFilter moimingLoginFilter(AuthenticationManager authenticationManager) {

        MoimingLoginFilter moimingLoginFilter = new MoimingLoginFilter();

        moimingLoginFilter.setAuthenticationManager(authenticationManager);
        moimingLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        moimingLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());

        return moimingLoginFilter;
    }

    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {

        return new JwtAuthenticationFilter(authenticationManager, userDetailsService(), moimingTokenProvider());
    }

    /*
     Http Security Configuration 인증 인가 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


        /**
         *  API에 대한 세부 권한 설정 ----- (추후 구체화 예정)
         *  1. API/AUTH 관련된 요청은 인가 예외 ((자동)로그인, 회원가입 등)
         *  2. 현재 그 외는 인증시 가능
         */
        http
                .antMatcher("/swagger-ui/**")
                .antMatcher("classpath:/META-INF/resources/webjars/swagger-ui/**")
                .antMatcher("/**")
                .antMatcher("/test-file/**")
                .antMatcher("/v3/api-docs/**").anonymous()
        ;

        // /api/v0/auth/~ 외의 모든 경로는 인증을 요구한다
        // 아직 특정 인가를 요구하는 경로는 없음 (ADMIN 단 미진행 중)
        http
//                .antMatcher(NetworkSetting.API_SERVER + "/**")
                .authorizeRequests()
                .antMatchers(NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH + "/**").permitAll()
//                .antMatchers("/api/datas/**").hasRole("USER")
//                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();



        /*
         인증가 인가를 수행할 필터 등록
         */
        http.apply(new MoimingSecurityFilterManager())
//        http
//
//                .addFilterAfter(moimingLoginFilter(), LogoutFilter.class)
//                .addFilterAfter(jwtAuthenticationFilter(), LogoutFilter.class)
//                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(moimingLoginFilter(), UsernamePasswordAuthenticationFilter.class)
        ;

        /**
         *  JWT 사용을 위한 기본 해제 설정
         */

        http
                .formLogin()
                .disable();

        http
                .httpBasic()
                .disable();


        http
                .csrf()
                .disable();

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        DefaultSecurityFilterChain filterChain = http.build();
        List<Filter> filters = filterChain.getFilters();
        for (Filter filter : filters) {
            System.out.println("filter.getClass() = " + filter.getClass());
        }

        return filterChain;
    }

}
