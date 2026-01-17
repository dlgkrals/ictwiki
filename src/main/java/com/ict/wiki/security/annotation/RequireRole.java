package com.ict.wiki.security.annotation;

import com.ict.wiki.login.domain.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드에 필요한 권한을 지정하는 애노테이션
 *
 * 사용 예시:
 * @RequireRole(Role.ADMIN)
 * @GetMapping("/admin/users")
 * public ResponseEntity<?> getUsers() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    Role[] value();  // 허용할 권한들
}
