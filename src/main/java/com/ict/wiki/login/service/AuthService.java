package com.ict.wiki.login.service;

import com.ict.wiki.login.domain.Role;
import com.ict.wiki.login.domain.User;
import com.ict.wiki.login.dto.LoginRequest;
import com.ict.wiki.login.dto.SignupRequest;
import com.ict.wiki.login.repository.UserRepository;
import com.ict.wiki.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;

    /**
     * 회원가입
     * @param request 회원가입 요청 정보
     */
    public void signup(SignupRequest request) {
        String fullEmail = request.getFullEmail();

        // 이메일 중복 체크
        if (userRepository.existsByEmail(fullEmail)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        // 사용자 생성
        User user = User.builder()
                .email(fullEmail)
                .password(PasswordUtil.encode(request.getPassword()))
                .name(request.getName())
                .studentId(request.getStudentId())
                .department(request.getDepartment())
                .role(Role.STUDENT)
                .active(true)
                .lastVerifiedDate(LocalDate.now()) // 가입 시 인증 완료로 처리
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인
     * @param request 로그인 요청 정보
     * @return 인증된 사용자 정보
     */
    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String fullEmail = request.getFullEmail();

        // 사용자 조회
        User user = userRepository.findByEmail(fullEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다"));

        // 비밀번호 확인
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 계정 활성화 확인
        if (!user.isActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다");
        }

        // 마지막 인증 날짜 확인 (3개월)
        if (user.getLastVerifiedDate() == null ||
                user.getLastVerifiedDate().isBefore(LocalDate.now().minusMonths(3))) {
            throw new IllegalArgumentException("계정 재인증이 필요합니다. 이메일을 확인해주세요");
        }

        return user;
    }

    /**
     * 이메일 중복 체크
     * @param emailPrefix 이메일 앞부분 (도메인 제외)
     * @return 중복 여부
     */
    @Transactional(readOnly = true)
    public boolean checkEmailDuplicate(String emailPrefix) {
        String fullEmail = emailPrefix + "@g.seoil.ac.kr";
        return userRepository.existsByEmail(fullEmail);
    }
}