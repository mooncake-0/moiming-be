//package com.peoplein.moiming.security.filter;
//
//import com.auth0.jwt.exceptions.JWTVerificationException;
//import com.auth0.jwt.exceptions.TokenExpiredException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.peoplein.moiming.model.ResponseBodyDto;
//import com.peoplein.moiming.security.OldJwtPropertySetting;
//import com.peoplein.moiming.security.domain.OldSecurityMember;
//import com.peoplein.moiming.security.exception.AuthErrorEnum;
//import com.peoplein.moiming.security.provider.token.MoimingTokenType;
//import com.peoplein.moiming.security.service.SecurityMemberService;
//import com.peoplein.moiming.security.token.JwtAuthenticationToken;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//
//@RequiredArgsConstructor
//@Slf4j
//public class OldJwtAuthenticationFilter extends OncePerRequestFilter {
//
//
//    private final OldMoimingTokenProvider oldMoimingTokenProvider;
//    private final UserDetailsService userDetailsService;
//    private ObjectMapper om = new ObjectMapper()
//            .registerModule(new JavaTimeModule())
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//    /*
//     JWT Token 이 요청에 있는지 확인하여 Verify 를 진행한다
//     Valid 일 경우 SecurityContextHolder 에 저장하여 Security 가 인가처리를 정상적으로 할 수 있도록 지원
//     */
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
//
//        String refreshToken = checkRefreshTokenHeader(request);
//
//        try {
//
//            if (StringUtils.hasText(refreshToken)) {
//
//                /*
//                 Refresh Token 을 검증한다
//                 검증시 DB 에 저장된 값을 조회하여 확인
//                 성공시 - 새 Refresh Token 을 발급하여 update. 새 Access Token 도 발급하여 Header 에 넣어주고, 기존 요청 처리를 위해 인가로 넘어간다
//                */
//
//                oldMoimingTokenProvider.validateToken(refreshToken, MoimingTokenType.JWT_RT);
//                String memberEmail = oldMoimingTokenProvider.retrieveEmail(refreshToken, MoimingTokenType.JWT_RT);
//
//                if (StringUtils.hasText(memberEmail)) {
//
//                    SecurityMemberService userDetailsService = (SecurityMemberService) this.userDetailsService;
//                    OldSecurityMember oldSecurityMember = (OldSecurityMember) userDetailsService.loadUserAndValidateRefreshToken(memberEmail, refreshToken);
//
//                    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(oldSecurityMember.getMember(), null, oldSecurityMember.getAuthorities());
//
//                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//
//                    String resetAccessToken = oldMoimingTokenProvider.generateToken(MoimingTokenType.JWT_AT, oldSecurityMember);
//                    response.addHeader(OldJwtPropertySetting.HEADER_AT, resetAccessToken);
//                    response.addHeader(OldJwtPropertySetting.HEADER_RT, oldSecurityMember.getMember().getRefreshToken());
//                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//                    filterChain.doFilter(request, response);
//
//                } else {
//                    log.error("JWT Token 내의 UID 를 찾을 수 없습니다");
//                    throw new JWTVerificationException("JWT Token 내의 UID 를 찾을 수 없습니다");
//                }
//
//
//            } else {
//
//            /*
//             Access Token 을 검증한다
//             검증시 JWT Library 를 통해 Validation 진행
//             성공시 - 인증 성공. 기존 요청 처리를 위해 인가로 넘어간다
//            */
//
//                String jwtToken = getJwtFromRequest(request);
//
//                if (StringUtils.hasText(jwtToken)) {
//
//                    oldMoimingTokenProvider.validateToken(jwtToken, MoimingTokenType.JWT_AT); // Validate 관련 에러는 모두 throw 로 날라간다
//                    String uid = oldMoimingTokenProvider.retrieveEmail(jwtToken, MoimingTokenType.JWT_AT);
//
//                    if (StringUtils.hasText(uid)) {
//
//                        OldSecurityMember oldSecurityMember = (OldSecurityMember) userDetailsService.loadUserByUsername(uid);
//                        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(oldSecurityMember.getMember(), null, oldSecurityMember.getAuthorities());
//                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//
//                    } else {
//                        log.error("JWT Token 내의 UID 를 찾을 수 없습니다");
//                        throw new JWTVerificationException("JWT Token 내의 UID 를 찾을 수 없습니다");
//                    }
//                }
//            }
//
//            filterChain.doFilter(request, response);
//
//        } catch (Exception exception) {
//
//            buildErrorResponse(request, response, exception);
//        }
//    }
//
//
//    /*
//     요청 헤더에 Authorization Bearer Token (ACCESS_TOKEN) 이 존재하는지 확인하고 반환한다
//     */
//    private String getJwtFromRequest(HttpServletRequest request) {
//
//        String authorizationToken = request.getHeader("Authorization");
//
//        if (StringUtils.hasText(authorizationToken) && authorizationToken.startsWith("Bearer ")) {
//            return authorizationToken.substring("Bearer ".length());
//        }
//
//        return null;
//    }
//
//    /*
//     요청 헤더에 REFRESH_TOKEN Header 가 존재하는지 확인하고 반환한다
//     */
//    private String checkRefreshTokenHeader(HttpServletRequest request) {
//
//        return (String) request.getHeader(OldJwtPropertySetting.HEADER_RT);
//    }
//
//    /*
//     해당 필터에서 발생하는 Exception 처리
//     ErrorResponse 를 Build 한 상태로 전달
//     */
//    private void buildErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
//
//        log.error("에러 발생 : " + e.getLocalizedMessage());
//
//        AuthErrorEnum authErrorEnum = null;
//
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setCharacterEncoding("UTF-8");
//
//        if (e instanceof TokenExpiredException) {
//
//            authErrorEnum = AuthErrorEnum.AUTH_JWT_EXPIRED;
//
//        } else if (e instanceof JWTVerificationException) {
//
//            authErrorEnum = AuthErrorEnum.AUTH_JWT_VERIFICATION_FAILED;
//
//        } else { // Authentication, AccessDenied Exception 제외 모든 에러는 여기서 종료된다
//            authErrorEnum = AuthErrorEnum.AUTH_UNKNOWN;
//        }
//
//        response.setStatus(authErrorEnum.getStatusCode());
//        // TODO :: 일단 SU
//
//        ResponseBodyDto<Object> errorResponseModel = ResponseBodyDto.createResponse(-1, "인증에 실패", null);
//
//        response.getWriter().write(om.writeValueAsString(errorResponseModel));
//    }
//}