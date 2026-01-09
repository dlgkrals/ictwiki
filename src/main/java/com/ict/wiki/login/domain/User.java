package com.ict.wiki.login.domain;

import com.ict.wiki.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // student@g.seoil.ac.kr

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String studentId; // 학번

    private String department; // 학과

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    private LocalDate lastVerifiedDate; // 마지막 인증 날짜

    // 비밀번호 업데이트
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 계정 비활성화
    public void deactivate() {
        this.active = false;
    }

    // 인증 완료 처리
    public void verify() {
        this.lastVerifiedDate = LocalDate.now();
    }
}
