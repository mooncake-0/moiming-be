package com.peoplein.moiming.security;


import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.security.auth.MoimingAuthenticationEntryPoint;
import com.peoplein.moiming.security.filter.MoimingAuthenticationFilter;
import com.peoplein.moiming.security.filter.MoimingLoginFilter;
import com.peoplein.moiming.security.handler.MoimingLoginFailureHandler;
import com.peoplein.moiming.security.handler.MoimingLoginSuccessHandler;
import com.peoplein.moiming.security.auth.MoimingAuthenticationProvider;
import com.peoplein.moiming.security.service.SecurityMemberService;
import com.peoplein.moiming.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityJwtConfig {

    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     Security 설정을 위한 Bean 설정
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new SecurityMemberService(memberRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new MoimingAuthenticationProvider(userDetailsService(), passwordEncoder);
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return new MoimingLoginSuccessHandler(authService);
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return new MoimingLoginFailureHandler();
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


    public MoimingAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {

        return new MoimingAuthenticationFilter(authenticationManager, userDetailsService(), authService);
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
                .antMatchers(AppUrlPath.API_SERVER + AppUrlPath.API_AUTH_VER + AppUrlPath.API_DOMAIN_AUTH + "/**").permitAll()
                .anyRequest().authenticated();


        /*
         인증가 인가를 수행할 필터 등록
         */
        http.apply(new MoimingSecurityFilterManager());
        http.exceptionHandling(manager -> manager
                        .authenticationEntryPoint(new MoimingAuthenticationEntryPoint())
//                .accessDeniedHandler(new MoimingAccessDeniedHandler()) // TODO :: 권한 예외 대기
        );


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

//        DefaultSecurityFilterChain filterChain = http.build();
//        List<Filter> filters = filterChain.getFilters();
//        for (Filter filter : filters) {
//            System.out.println("filter.getClass() = " + filter.getClass());
//        }

        return http.build();
    }

}
