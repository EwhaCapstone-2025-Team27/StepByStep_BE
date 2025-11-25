package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.UserStatus;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.exception.DuplicateEmailException;
import com.dragon.stepbystep.exception.DuplicateNicknameException;
import com.dragon.stepbystep.exception.UserDeletedException;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.UserBadgeRepository;
import com.dragon.stepbystep.repository.UserRepository;
import com.dragon.stepbystep.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String TEMP_PASSWORD_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MailService mailService;
    private final UserBadgeRepository userBadgeRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.temp-password-expiration-minutes:30}")
    private long tempPasswordExpirationMinutes;

    @Value("${app.auth.temp-password-length:12}")
    private int tempPasswordLength;

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    // 회원가입
    public UserResponseDto registerUser(UserRegisterDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("모든 항목을 입력해주세요.");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()
                || dto.getPasswordConfirm() == null || dto.getPasswordConfirm().isBlank()
                || dto.getNickname() == null || dto.getNickname().isBlank()
                || dto.getGender() == null
                || dto.getBirthyear() == null) {
            throw new IllegalArgumentException("모든 항목을 입력해주세요.");
        }

        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException();
        }

        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new DuplicateNicknameException();
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(encodedPassword);
        User savedUser = userRepository.save(user);

        return UserResponseDto.fromEntity(savedUser);
    }

    // 사용자 정보 조회
    @Transactional
    public UserResponseDto getMe(Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new UserNotFoundException();
        }

        return UserResponseDto.fromEntity(user, getUserBadges(id));
    }

    // 사용자 정보 수정(닉네임, 성별, 출생년도)
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("수정할 정보를 입력해주세요.");
        }

        User target = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        boolean hasNickname = dto.getNickname() != null;
        boolean hasGender = dto.getGender() != null;
        boolean hasBirthyear = dto.getBirthyear() != null;

        if (!(hasNickname || hasGender || hasBirthyear)) {
            throw new IllegalArgumentException("수정할 정보를 입력해주세요.");
        }

        if (hasNickname) {
            String nickname = dto.getNickname();
            if (nickname.isBlank()) {
                throw new IllegalArgumentException("닉네임을 입력해주세요.");
            }
            if (!nickname.matches("^[ㄱ-ㅎ가-힣A-Za-z0-9]{3,10}$")) {
                throw new IllegalArgumentException("닉네임은 3~10자, 한국어/영어/숫자만 가능합니다.");
            }
            if (!nickname.equals(target.getNickname()) && userRepository.existsByNickname(nickname)) {
                throw new DuplicateNicknameException();
            }
            target.setNickname(nickname);
        }
        if (hasGender) {
            target.setGender(dto.getGender());
        }
        if (hasBirthyear) {
            Integer birthyear = dto.getBirthyear();
            if (birthyear < 1900 || birthyear > 2100) {
                throw new IllegalArgumentException("출생년도는 1900~2100 사이의 숫자만 가능합니다.");
            }
            target.setBirthyear(birthyear);
        }

        User updated = userRepository.save(target);
        return UserResponseDto.fromEntity(updated, getUserBadges(id));
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long id, ChangePwRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        boolean allowWithoutCurrentPassword = user.isMustChangePassword() && user.getTempPasswordHash() != null;
        String currentPassword = dto.getCurrentPassword();

        if (!allowWithoutCurrentPassword) {
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
            }
        } else if (currentPassword != null && !currentPassword.isBlank()) {
            boolean matchesPermanent = passwordEncoder.matches(currentPassword, user.getPasswordHash());
            boolean matchesTemporary = user.getTempPasswordHash() != null
                    && passwordEncoder.matches(currentPassword, user.getTempPasswordHash());
            if (!(matchesPermanent || matchesTemporary)) {
                throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
            }
        }

        if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalStateException("새 비밀번호가 기존 비밀번호와 동일합니다.");
        }

        if (user.getTempPasswordHash() != null
                && passwordEncoder.matches(dto.getNewPassword(), user.getTempPasswordHash())) {
            throw new IllegalStateException("임시 비밀번호와 동일합니다.");
        }

        String newPasswordHash = passwordEncoder.encode(dto.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        user.incrementTokenVersion();
        user.setMustChangePassword(false);
        user.setTempPasswordHash(null);
        user.setTempPasswordExpiresAt(null);
        user.setTempPasswordIssuedAt(null);
        if (user.getStatus() == UserStatus.RESET_REQUIRED) {
            user.setStatus(UserStatus.ACTIVE);
        }
    }

    @Transactional
    public void issueTemporaryPassword(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException();
        }

        String temporaryPassword = generateTemporaryPassword();

        LocalDateTime issuedAt = LocalDateTime.now();
        user.setTempPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setTempPasswordExpiresAt(issuedAt.plusMinutes(tempPasswordExpirationMinutes));
        user.setMustChangePassword(true);
        user.setTempPasswordIssuedAt(issuedAt);
        user.setStatus(UserStatus.RESET_REQUIRED);

        userRepository.save(user);

        mailService.sendTemporaryPasswordEmail(email, temporaryPassword, tempPasswordExpirationMinutes);
    }

    // 임시 비밀번호 생성
    private String generateTemporaryPassword() {
        if (tempPasswordLength <= 0) {
            throw new IllegalStateException("임시 비밀번호 길이가 유효하지 않습니다.");
        }

        StringBuilder builder = new StringBuilder(tempPasswordLength);
        for (int i = 0; i < tempPasswordLength; i++) {
            int index = secureRandom.nextInt(TEMP_PASSWORD_CHARACTERS.length());
            builder.append(TEMP_PASSWORD_CHARACTERS.charAt(index));
        }

        return builder.toString();
    }

    private List<UserBadgeResponseDto> getUserBadges(Long userId) {
        return userBadgeRepository.findBadgeResponsesByUserId(userId);
    }

    // 로그인
    @Transactional
    public TokenDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException();
        }

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
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 임시 비번 로그인 성공 시, 비번 변경 강제 플래그 유지/설정
        if (tempOK) {
            user.setMustChangePassword(true);
        }

        String access = jwtTokenProvider.createAccessToken(user.getId());   // 필요시 claims 오버로드 사용
        String refresh = jwtTokenProvider.createRefreshToken(user.getId());

        return new TokenDto(access, refresh, user.isMustChangePassword());
    }
    

    // 이메일 찾기
    @Transactional
    public FindEmailResponseDto findEmail(FindEmailRequestDto dto) {
        User user = userRepository.findByNicknameAndGenderAndBirthyear(
                        dto.getNickname(), dto.getGender(), dto.getBirthyear()
                )
                .orElseThrow(() -> new UserNotFoundException("일치하는 사용자를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new UserDeletedException();
        }

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