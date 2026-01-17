package com.ict.wiki.config;

import com.ict.wiki.security.interceptor.RoleCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RoleCheckInterceptor roleCheckInterceptor;

    /**
     * 보안 헤더 필터 등록
     */
    @Bean
    public FilterRegistrationBean<SecurityHeaderFilter> securityHeaderFilter() {
        FilterRegistrationBean<SecurityHeaderFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new SecurityHeaderFilter());
        registrationBean.addUrlPatterns("/*"); // 모든 경로에 적용
        registrationBean.setOrder(0); // 가장 먼저 실행

        return registrationBean;
    }

    /**
     * 세션 인증 필터 등록
     */
    @Bean
    public FilterRegistrationBean<SessionAuthenticationFilter> sessionAuthFilter() {
        FilterRegistrationBean<SessionAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new SessionAuthenticationFilter());
        registrationBean.addUrlPatterns("/api/*"); // /api로 시작하는 모든 경로에 적용
        registrationBean.setOrder(1); // 필터 우선순위

        return registrationBean;
    }

    /**
     * 권한 체크 인터셉터 등록
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleCheckInterceptor)
                .addPathPatterns("/api/**")  // 모든 API에 적용
                .order(1);
    }
}