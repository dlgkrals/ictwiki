package com.ict.wiki.config;

import com.ict.wiki.security.interceptor.RoleCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RoleCheckInterceptor roleCheckInterceptor;

    /**
     * CORS 필터 등록 (최우선)
     * - 프론트엔드(localhost:5173)에서 API 호출 허용
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 자격 증명 허용 (쿠키, 세션)
        config.setAllowCredentials(true);

        // 허용할 Origin
        config.setAllowedOrigins(Arrays.asList(
                "https://www.ictwiki.site",
                "http://localhost:5173",
                "http://localhost:4173",
                "http://127.0.0.1:5173"
        ));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용할 헤더
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-CSRF-TOKEN"
        ));

        // 노출할 헤더
        config.setExposedHeaders(Arrays.asList(
                "X-CSRF-TOKEN"
        ));

        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0); // 가장 먼저 실행
        return bean;
    }

    /**
     * 보안 헤더 필터 등록
     */
    @Bean
    public FilterRegistrationBean<SecurityHeaderFilter> securityHeaderFilter() {
        FilterRegistrationBean<SecurityHeaderFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new SecurityHeaderFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);

        return registrationBean;
    }

    /**
     * XSS 방지 필터 등록
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter() {
        FilterRegistrationBean<XssFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new XssFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);

        return registrationBean;
    }

    /**
     * CSRF 방지 필터 등록
     */
    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilter() {
        FilterRegistrationBean<CsrfFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new CsrfFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(3);

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
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(4);

        return registrationBean;
    }

    /**
     * 권한 체크 인터셉터 등록
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleCheckInterceptor)
                .addPathPatterns("/api/**")
                .order(1);
    }
}