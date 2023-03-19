package com.peoplein.moiming.security;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.security.filter.JwtAuthenticationFilter;
import com.peoplein.moiming.security.filter.JwtLoginFilter;
import com.peoplein.moiming.security.handler.JwtLoginFailureHandler;
import com.peoplein.moiming.security.handler.JwtLoginSuccessHandler;
import com.peoplein.moiming.security.provider.JwtAuthenticationProvider;
import com.peoplein.moiming.security.provider.token.JwtTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.service.SecurityMemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class SecurityJwtConfig extends WebSecurityConfigurerAdapter {

    /*
     AuthenticationManager 주입을 위해 Override
    */
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

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
        return new JwtLoginSuccessHandler(moimingTokenProvider(), (SecurityMemberService) userDetailsService());
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return new JwtLoginFailureHandler();
    }

    @Bean
    public MoimingTokenProvider moimingTokenProvider() {
        return new JwtTokenProvider();
    }

    @Bean
    public JwtLoginFilter jwtLoginFilter() throws Exception {

        JwtLoginFilter jwtLoginFilter = new JwtLoginFilter();

        jwtLoginFilter.setAuthenticationManager(authenticationManagerBean());
        jwtLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        jwtLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());

        return jwtLoginFilter;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {

        return new JwtAuthenticationFilter(moimingTokenProvider(), userDetailsService());
    }

    /*
     Http Security Configuration 인증 인가 설정
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {


        /**
         *  API에 대한 세부 권한 설정 ----- (추후 구체화 예정)
         *  1. API/AUTH 관련된 요청은 인가 예외 ((자동)로그인, 회원가입 등)
         *  2. 현재 그 외는 인증시 가능
         */
        http
                .antMatcher("/swagger-ui")
        ;

        http
                .antMatcher(NetworkSetting.API_SERVER + "/**")
                .authorizeRequests()
                .antMatchers(NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH + "/**").permitAll()
//                .antMatchers("/api/datas/**").hasRole("USER")
//                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();


        /*
         인증가 인가를 수행할 필터 등록
         */
        http
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtLoginFilter(), UsernamePasswordAuthenticationFilter.class)
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

    }
}
