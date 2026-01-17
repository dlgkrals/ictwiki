package com.ict.wiki.config;

import com.ict.wiki.util.XssUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * XSS(Cross-Site Scripting) 방지 필터
 * 모든 요청 파라미터를 필터링하여 악성 스크립트를 제거합니다.
 */
@Slf4j
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // XSS 방지 Request Wrapper 적용
        XssRequestWrapper wrappedRequest = new XssRequestWrapper(httpRequest);

        chain.doFilter(wrappedRequest, response);
    }

    /**
     * XSS 방지를 위한 Request Wrapper
     * 모든 파라미터 값을 필터링합니다.
     */
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter);

            if (values == null) {
                return null;
            }

            String[] encodedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                encodedValues[i] = filterXss(values[i]);
            }

            return encodedValues;
        }

        @Override
        public String getParameter(String parameter) {
            String value = super.getParameter(parameter);
            return filterXss(value);
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return filterXss(value);
        }

        /**
         * XSS 필터링
         */
        private String filterXss(String value) {
            if (value == null || value.isEmpty()) {
                return value;
            }

            // 스크립트 태그 제거
            String filtered = XssUtil.removeScriptTags(value);

            // HTML 이스케이핑은 상황에 따라 선택적으로 적용
            // 일반 텍스트 입력에는 이스케이핑 적용
            // (JSON body는 별도 처리 필요)

            return filtered;
        }
    }
}
