package com.dragon.stepbystep.service;

import com.dragon.stepbystep.common.RandomPasswordGenerator;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.UserStatus;
import com.dragon.stepbystep.dto.FindPasswordRequestDto;
import com.dragon.stepbystep.exception.UserDeletedException;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int EXPIRE_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    // 30분 만료, 12자 이상(영대/영소/숫자/특수) 규칙은 생성기로 충족
    private static final int TEMP_LEN = 12;
    private static final int TTL_MINUTES = 30;

    public void issueTemporaryPassword(FindPasswordRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException();
        }

        LocalDateTime issuedAt = LocalDateTime.now();
        String temporaryPassword = RandomPasswordGenerator.generate(12);
        user.setTempPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setTempPasswordIssuedAt(issuedAt);
        user.setTempPasswordExpiresAt(issuedAt.plusMinutes(EXPIRE_MINUTES));
        user.setMustChangePassword(true);
        userRepository.save(user);

        mailService.sendTemporaryPasswordEmail(user.getEmail(), temporaryPassword, EXPIRE_MINUTES);
    }

    @Transactional
    public void issueTemporaryPassword(FindPasswordRequestDto dto) {
        // 이메일만으로 찾는 정책(보안상 존재여부 노출 주의. 현재는 그대로 진행)
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입된 이메일이 없습니다."));

        String temp = RandomPasswordGenerator.generate(TEMP_LEN);

        user.setTempPasswordHash(passwordEncoder.encode(temp));
        user.setTempPasswordIssuedAt(LocalDateTime.now());
        user.setTempPasswordExpiresAt(LocalDateTime.now().plusMinutes(TTL_MINUTES));

        // 임시비번 발급 시 로그인하면 반드시 변경하도록
        user.setMustChangePassword(true);
        user.setStatus(UserStatus.RESET_REQUIRED);

        // 로그인 세션/토큰 무효화를 원하면 token_version 증가
        user.setTokenVersion(user.getTokenVersion() + 1);

        // 메일 발송(로컬에선 실패해도 API는 계속가도록 MailService에서 처리)
        mailService.sendTemporaryPasswordEmail(user.getEmail(), temp);
    }

    public boolean isTempPasswordValid(User user, String rawPassword) {
        if (user.getTempPasswordHash() == null) return false;
        if (user.getTempPasswordExpiresAt() == null) return false;
        if (LocalDateTime.now().isAfter(user.getTempPasswordExpiresAt())) return false;
        return passwordEncoder.matches(rawPassword, user.getTempPasswordHash());
    }

    @Transactional
    public void clearTemporaryPassword(User user) {
        user.setTempPasswordHash(null);
        user.setTempPasswordIssuedAt(null);
        user.setTempPasswordExpiresAt(null);
        // mustChangePassword는 비밀번호 변경 완료 시점에 false로 전환
    }

}
