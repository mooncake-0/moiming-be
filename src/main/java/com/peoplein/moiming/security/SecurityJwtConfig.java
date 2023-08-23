package com.peoplein.moiming.security;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.security.filter.JwtAuthenticationFilter;
import com.peoplein.moiming.security.filter.MoimingLoginFilter;
import com.peoplein.moiming.security.handler.ExceptionFilterHandler;
import com.peoplein.moiming.security.handler.MoimingLoginFailureHandler;
import com.peoplein.moiming.security.handler.MoimingLoginSuccessHandler;
import com.peoplein.moiming.security.auth.JwtAuthenticationProvider;
import com.peoplein.moiming.security.token.JwtTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.service.SecurityMemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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
         Swagger 와 충돌이 나는 것으로 추정
         - 일단 이렇게 해놓겠음
         */
        http
                .authorizeRequests()
                .antMatchers("/static/css/**, /static/js/**, *.ico").permitAll()
                .antMatchers("/swagger-ui.html", "/v2/**", "/configuration/ui",
                        "/swagger-resources/**", "/configuration/security",
                        "/swagger-ui/**", "/webjars/**", "/swagger/**").permitAll()
                .antMatchers(NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH + "/**").permitAll()
                .anyRequest().authenticated();


        /*
         인증가 인가를 수행할 필터 등록
         */
        http.apply(new MoimingSecurityFilterManager());

        // 인증 예외 - AuthenticationEntryPoint 가 크게 하는 일이 없어서 lamda 로 일단 정의 - 추후 exception 별 로깅 이런 처리가 필요시 Custom 활 필요
        http.exceptionHandling().authenticationEntryPoint((req, resp, exception)->{
            ExceptionFilterHandler.sendExceptionResponse(resp, exception.getMessage(), HttpStatus.UNAUTHORIZED);
        });

        // 인가 예외 - AccessDeniedHadnler 역시 마찬가지, 할 일이 403 뿐
        http.exceptionHandling().accessDeniedHandler((req, resp, exception) -> {
            ExceptionFilterHandler.sendExceptionResponse(resp, exception.getMessage(), HttpStatus.FORBIDDEN);
        });


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
