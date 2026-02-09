package com.ict.wiki.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 이메일 인증 메일 발송 (30분 유효)
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "[ICT Wiki] 이메일 인증을 완료해주세요";
        String verificationUrl = baseUrl + "/verify-email?token=" + token;

        String content = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #007bff;">ICT Wiki 회원가입을 환영합니다!</h2>
                        <p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요.</p>
                        <p style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
                                이메일 인증하기
                            </a>
                        </p>
                        <p style="color: #666; font-size: 14px;">또는 아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>
                        <p style="background-color: #f5f5f5; padding: 10px; border-radius: 3px; word-break: break-all; font-size: 12px;">
                            %s
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #ff6b6b; font-size: 13px; font-weight: bold;">
                            ⚠️ 이 링크는 30분 동안만 유효합니다.
                        </p>
                        <p style="color: #999; font-size: 12px;">
                            ※ 만료된 경우 회원가입 페이지에서 재발송할 수 있습니다.<br>
                            ※ 본인이 요청하지 않았다면 이 메일을 무시하세요.
                        </p>
                    </div>
                </body>
                </html>
                """, verificationUrl, verificationUrl);

        sendHtmlEmail(toEmail, subject, content);
    }

    /**
     * 비밀번호 재설정 메일 발송 (1시간 유효)
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "[ICT Wiki] 비밀번호 재설정";
        String resetUrl = baseUrl + "/reset-password?token=" + token;  // ⭐ 프론트엔드 URL

        String content = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #dc3545;">비밀번호 재설정 요청</h2>
                        <p>비밀번호 재설정을 요청하셨습니다.</p>
                        <p>아래 버튼을 클릭하여 새 비밀번호를 설정해주세요.</p>
                        <p style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #dc3545; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
                                비밀번호 재설정하기
                            </a>
                        </p>
                        <p style="color: #666; font-size: 14px;">또는 아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>
                        <p style="background-color: #f5f5f5; padding: 10px; border-radius: 3px; word-break: break-all; font-size: 12px;">
                            %s
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #ff6b6b; font-size: 13px; font-weight: bold;">
                            ⚠️ 이 링크는 1시간 동안만 유효합니다.
                        </p>
                        <p style="color: #999; font-size: 12px;">
                            ※ 본인이 요청하지 않았다면 이 메일을 무시하세요.<br>
                            ※ 비밀번호는 절대 타인에게 공유하지 마세요.
                        </p>
                    </div>
                </body>
                </html>
                """, resetUrl, resetUrl);

        sendHtmlEmail(toEmail, subject, content);
    }

    /**
     * 3개월 재인증 알림 메일
     */
    public void sendReVerificationEmail(String toEmail, String name) {
        String subject = "[ICT Wiki] 계정 재인증이 필요합니다";
        String loginUrl = baseUrl + "/login";

        String content = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #28a745;">%s님, 계정 재인증이 필요합니다</h2>
                        <p>마지막 인증 이후 3개월이 경과했습니다.</p>
                        <p>계속 사용하시려면 로그인하여 계정을 재인증해주세요.</p>
                        <p style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
                                로그인하기
                            </a>
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            ※ 7일 내 재인증하지 않으면 계정이 비활성화됩니다.<br>
                            ※ 재인증은 로그인만 하시면 자동으로 완료됩니다.
                        </p>
                    </div>
                </body>
                </html>
                """, name, loginUrl);

        sendHtmlEmail(toEmail, subject, content);
    }

    /**
     * HTML 이메일 발송
     */
    private void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // HTML 허용

            mailSender.send(message);
            log.info("이메일 발송 성공 - To: {}, Subject: {}", to, subject);

        } catch (MessagingException e) {
            log.error("이메일 발송 실패 - To: {}, Error: {}", to, e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }
}