package com.dragon.stepbystep.service;

import com.dragon.stepbystep.common.RandomPasswordGenerator;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.dto.FindPasswordRequestDto;
import com.dragon.stepbystep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int EXPIRE_MINUTES = 30;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private  MailService mailService;

    public PasswordResetService(FindPasswordRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

        String temp = RandomPasswordGenerator.generate(12);
        user.setTempPasswordHash(passwordEncoder.encode(temp));
        user.setTempPasswordExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
        user.setMustChangePassword(true);
        userRepository.save(user);

        mailService.sendTempPasswordMail(user.getEmail(), temp, EXPIRE_MINUTES);
    }
}
