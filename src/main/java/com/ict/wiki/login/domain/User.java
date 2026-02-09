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

    private String phoneNumber; // 핸드폰 번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = false;

    /**
     * 관리자 승인 여부
     * - 회원가입 시 false
     * - 관리자 승인 후 true
     */
    @Column(nullable = false)
    private boolean approved = false;

    // 비밀번호 업데이트
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 계정 활성화
    public void activate() {
        this.active = true;
    }


    // 계정 비활성화
    public void deactivate() {
        this.active = false;
    }

    // 인증 완료 처리
    public void verify() {
        this.active = true;
    }

    //사용자 권한 변경
    public void updateRole(Role newRole) {
        this.role = newRole;
    }

    //이름 업데이트
    public void updateName(String name) {
        this.name = name;
    }

    //핸드폰 번호 업데이트
    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * 회원 승인
     */
    public void approve() {
        this.approved = true;
    }

    /**
     * 회원 승인 거부
     */
    public void reject() {
        this.approved = false;
    }

    /**
     * 관리자가 강제로 비밀번호 변경
     */
    public void changePasswordByAdmin(String newPassword) {
        this.password = newPassword;
    }
}
