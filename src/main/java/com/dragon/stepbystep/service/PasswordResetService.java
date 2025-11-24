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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    // 30분 만료, 12자 이상(영대/영소/숫자/특수) 규칙은 생성기로 충족
    private static final int TEMP_LEN = 12;
    private static final int EXPIRE_MINUTES = 30;

    @Transactional
    public void issueTemporaryPassword(FindPasswordRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException();
        }

        String temporaryPassword = RandomPasswordGenerator.generate(TEMP_LEN);
        LocalDateTime issuedAt = LocalDateTime.now();

        user.setTempPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setTempPasswordIssuedAt(issuedAt);
        user.setTempPasswordExpiresAt(issuedAt.plusMinutes(EXPIRE_MINUTES));
        user.setMustChangePassword(true);
        user.setStatus(UserStatus.RESET_REQUIRED);
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        mailService.sendTemporaryPasswordEmail(user.getEmail(), temporaryPassword, EXPIRE_MINUTES);
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