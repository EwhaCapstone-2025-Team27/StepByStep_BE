package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.UserStatus;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.UserRepository;
import com.dragon.stepbystep.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public boolean existsByEmail(String userEmail) {
        return userRepository.existsById(userEmail);
    }

    // 사용자 조회
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new UserNotFoundException();
        }

        return UserResponseDto.fromEntity(user);
    }

    // 사용자 등록
    public UserResponseDto registerUser(UserRegisterDto dto) {

        if (dto.getEmail() == null || dto.getPassword() == null || dto.getNickname() == null || dto.getGender() == null || dto.getBirthyear() == null) {
            throw new IllegalArgumentException("필수 값이 누락되었습니다.");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(encodedPassword);
        User registered = userRepository.save(user);
        return UserResponseDto.fromEntity(registered);
    }

    // 사용자 수정
    @Transactional
    public UserResponseDto updateUser(String email, UserUpdateDto dto) {
        User target = userRepository.findById(email)
                .orElseThrow(UserNotFoundException::new);


        if (dto.getNickname() != null)  target.setNickname(dto.getNickname());
        if (dto.getGender() != null)    target.setGender(dto.getGender());
        if (dto.getBirthyear() != null) target.setBirthyear(dto.getBirthyear());


        boolean wantsPwChange =
                dto.getCurrentPassword() != null ||
                        dto.getNewPassword() != null ||
                        dto.getNewPasswordConfirm() != null;

        if (wantsPwChange) {

            if (isBlank(dto.getCurrentPassword()) ||
                    isBlank(dto.getNewPassword()) ||
                    isBlank(dto.getNewPasswordConfirm())) {
                throw new IllegalArgumentException("비밀번호 변경에는 current/new/confirm 모두 필요합니다.");
            }

            if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
            }

            if (!passwordEncoder.matches(dto.getCurrentPassword(), target.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            if (passwordEncoder.matches(dto.getNewPassword(), target.getPassword())) {
                throw new IllegalStateException("기존 비밀번호와 동일합니다.");
            }

            validatePasswordPolicy(dto.getNewPassword());


            target.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        User updated = userRepository.save(target);
        return UserResponseDto.fromEntity(updated);
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private void validatePasswordPolicy(String pw) {
        if (pw.length() < 8 || pw.length() > 20)
            throw new IllegalArgumentException("비밀번호는 8~20자여야 합니다.");
    }

    // 사용자 삭제
    @Transactional
    public void deleteUser(String email) {
        User target = userRepository.findById(email)
                .orElseThrow(UserNotFoundException::new);

        if (target.getStatus() == UserStatus.DELETED) return;

        target.setStatus(UserStatus.DELETED);
    }

    public TokenDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);

        if (user == null) {
            throw new UserNotFoundException();
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("잘못된 비밀번호 입니다.");
        }

        return new TokenDto(
                jwtTokenProvider.createAccessToken(user.getEmail()),
                jwtTokenProvider.createRefreshToken(user.getEmail())
        );
    }

}