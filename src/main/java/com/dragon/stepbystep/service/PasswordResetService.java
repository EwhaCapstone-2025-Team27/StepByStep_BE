package com.dragon.stepbystep.service;

import com.dragon.stepbystep.common.RandomPasswordGenerator;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.UserStatus;
import com.dragon.stepbystep.dto.FindPasswordRequestDto;
import com.dragon.stepbystep.exception.UserDeletedException;
import com.dragon.stepbystep.exception.UserNotFoundException;
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
}
