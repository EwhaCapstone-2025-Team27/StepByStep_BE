package com.dragon.stepbystep.service;

import com.dragon.stepbystep.common.RandomPasswordGenerator;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.dto.FindPasswordRequestDto;
import com.dragon.stepbystep.repository.UserRepository;
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

    public void issueTemporaryPassword(FindPasswordRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

        LocalDateTime issuedAt = LocalDateTime.now();
        String temporaryPassword = RandomPasswordGenerator.generate(12);
        user.setTempPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setTempPasswordIssuedAt(issuedAt);
        user.setTempPasswordExpiresAt(issuedAt.plusMinutes(EXPIRE_MINUTES));
        user.setMustChangePassword(true);
        userRepository.save(user);

        mailService.sendTemporaryPasswordEmail(user.getEmail(), temporaryPassword, EXPIRE_MINUTES);
    }
}
