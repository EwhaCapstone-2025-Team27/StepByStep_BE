package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.UserStatus;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.UserRepository;
import com.dragon.stepbystep.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String TEMP_PASSWORD_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MailService mailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.temp-password-expiration-minutes:30}")
    private long tempPasswordExpirationMinutes;

    @Value("${app.auth.temp-password-length:12}")
    private int tempPasswordLength;

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    // 회원가입
    // 사용자 등록
    public UserResponseDto registerUser(UserRegisterDto dto) {

        if (dto.getEmail() == null || dto.getPassword() == null || dto.getNickname() == null || dto.getGender() == null || dto.getBirthyear() == null) {
            throw new IllegalArgumentException("필수 값이 누락되었습니다.");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }


    }

    // 사용자 정보 조회
    @Transactional
    public UserResponseDto getMe(Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new UserNotFoundException();
        }

        return UserResponseDto.fromEntity(user);
    }

    // 사용자 정보 수정(닉네임, 성별, 출생년도)
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        User user = dto.toEntity();
        System.out.println(user.toString());

        User target = userRepository.findById(id).orElse(null);
        if (target == null || id != user.getId()) {
            throw new UserNotFoundException();
        }
        target.patch(user);

        User updated = userRepository.save(target);
        return UserResponseDto.fromEntity(updated);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long id, ChangePwRequestDto dto) {
        User user = userRepository.findById(id).orElse(null);

        // 현재 비밀번호 확인
        if(!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())){
            throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 일치 확인
        if(!dto.getNewPassword().equals(dto.getNewPasswordConfirm())){
            throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
        }

        // 기존 비밀번호와 동일 금지
        if(passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())){
            throw new IllegalStateException("기존 비밀번호와 동일합니다.");
        }

        // 새 비밀번호 해시 & 저장
        String newPasswordHash = passwordEncoder.encode(dto.getNewPassword());
        user.setPasswordHash(newPasswordHash);

        // 기존 토큰 무효화
        user.incrementTokenVersion();
    }

    @Transactional
    public void issueTemporaryPassword(String email) {
        if (isBlank(email)) {
            throw new IllegalArgumentException("이메일은 필수 값입니다.");
        }

        User user = userRepository.findById(email)
                .orElseThrow(UserNotFoundException::new);

        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalStateException("탈퇴한 사용자입니다.");
        }

        String temporaryPassword = generateTemporaryPassword();

        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setStatus(UserStatus.RESET_REQUIRED);
        user.setTempPasswordIssuedAt(LocalDateTime.now());

        mailService.sendTemporaryPasswordEmail(email, temporaryPassword, tempPasswordExpirationMinutes);
    }

    // 로그인
    @Transactional
    public TokenDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(UserNotFoundException::new);

        boolean normalOK = passwordEncoder.matches(dto.getPassword(), user.getPasswordHash());
        boolean tempOK = false;

        // 일반 비번이 틀렸고, 임시 비번/만료가 세팅되어 있을 때만 검사
        if (!normalOK && user.getTempPasswordHash() != null && user.getTempPasswordExpiresAt() != null) {
            // 만료 체크
            boolean notExpired = LocalDateTime.now().isBefore(user.getTempPasswordExpiresAt());
            if (notExpired) {
                tempOK = passwordEncoder.matches(dto.getPassword(), user.getTempPasswordHash());
            } else {
                // 만료됐으면 깨끗이 비워두는 편이 운영상 깔끔
                user.setTempPasswordHash(null);
                user.setTempPasswordExpiresAt(null);
                userRepository.save(user);
            }
        }

        if (!(normalOK || tempOK)) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 임시 비번 로그인 성공 시, 비번 변경 강제 플래그 유지/설정
        if (tempOK) {
            user.setMustChangePassword(true);
            // 1회성
            user.setTempPasswordHash(null);
            user.setTempPasswordExpiresAt(null);
            userRepository.save(user);
        }

        String access = jwtTokenProvider.createAccessToken(user.getId());   // 필요시 claims 오버로드 사용
        String refresh = jwtTokenProvider.createRefreshToken(user.getId());

        return new TokenDto(access, refresh, user.isMustChangePassword());
    }

    // 이메일 찾기
    @Transactional
    public FindEmailResponseDto findEmail(FindEmailRequestDto dto) {
        User user = userRepository.findByNicknameAndGenderAndBirthyearAndStatus(
                        dto.getNickname(), dto.getGender(), dto.getBirthyear(), UserStatus.ACTIVE
                )
                .orElseThrow(UserNotFoundException::new);

        return FindEmailResponseDto.fromEntity(user);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(Long id) {
        User target = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        if (target.getStatus() == UserStatus.DELETED) return;

        target.setStatus(UserStatus.DELETED);
        target.setTempPasswordIssuedAt(null);
    }

}