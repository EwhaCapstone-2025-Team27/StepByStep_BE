package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.GenderType;
import com.dragon.stepbystep.domain.enums.UserStatus;
import com.dragon.stepbystep.dto.ChangePwRequestDto;
import com.dragon.stepbystep.dto.LoginRequestDto;
import com.dragon.stepbystep.dto.TokenDto;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.repository.UserRepository;
import com.dragon.stepbystep.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final long USER_ID = 1L;
    private static final String EMAIL = "tester@example.com";
    private static final String CURRENT_PASSWORD_HASH = "encoded-current";
    private static final String TEMP_PASSWORD_HASH = "encoded-temp";
    private static final String TEMP_PASSWORD = "TempPass123!";
    private static final String NEW_PASSWORD = "NewPass456!";
    private static final String NEW_PASSWORD_HASH = "encoded-new";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MailService mailService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash(CURRENT_PASSWORD_HASH)
                .nickname("tester")
                .gender(GenderType.M)
                .birthyear(1990)
                .status(UserStatus.RESET_REQUIRED)
                .tempPasswordHash(TEMP_PASSWORD_HASH)
                .tempPasswordExpiresAt(LocalDateTime.now().plusMinutes(30))
                .tempPasswordIssuedAt(LocalDateTime.now())
                .mustChangePassword(true)
                .build();
    }

    @Test
    void temporaryPasswordLoginAndChangePasswordFlow() {
        LoginRequestDto loginRequest = new LoginRequestDto(EMAIL, TEMP_PASSWORD);

        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEMP_PASSWORD, CURRENT_PASSWORD_HASH)).willReturn(false);
        given(passwordEncoder.matches(TEMP_PASSWORD, TEMP_PASSWORD_HASH)).willReturn(true);
        given(jwtTokenProvider.createAccessToken(USER_ID)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(USER_ID)).willReturn("refresh-token");

        TokenDto tokenDto = userService.login(loginRequest);

        assertThat(tokenDto.isForcePasswordChange()).isTrue();
        assertThat(user.getTempPasswordHash()).isEqualTo(TEMP_PASSWORD_HASH);
        assertThat(user.isMustChangePassword()).isTrue();

        ChangePwRequestDto changeRequest = new ChangePwRequestDto();
        changeRequest.setNewPassword(NEW_PASSWORD);
        changeRequest.setNewPasswordConfirm(NEW_PASSWORD);

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(NEW_PASSWORD, CURRENT_PASSWORD_HASH)).willReturn(false);
        given(passwordEncoder.matches(NEW_PASSWORD, TEMP_PASSWORD_HASH)).willReturn(false);
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(NEW_PASSWORD_HASH);

        userService.changePassword(USER_ID, changeRequest);

        assertThat(user.getPasswordHash()).isEqualTo(NEW_PASSWORD_HASH);
        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.getTempPasswordHash()).isNull();
        assertThat(user.getTempPasswordExpiresAt()).isNull();
        assertThat(user.getTempPasswordIssuedAt()).isNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getTokenVersion()).isEqualTo(1);
    }

    @Test
    void changePasswordRequiresCurrentPasswordWhenNotUsingTemporary() {
        user.setMustChangePassword(false);
        user.setTempPasswordHash(null);
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

        ChangePwRequestDto changeRequest = new ChangePwRequestDto();
        changeRequest.setNewPassword(NEW_PASSWORD);
        changeRequest.setNewPasswordConfirm(NEW_PASSWORD);

        assertThatThrownBy(() -> userService.changePassword(USER_ID, changeRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("현재 비밀번호");

        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void changePasswordThrowsWhenUserMissing() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        ChangePwRequestDto changeRequest = new ChangePwRequestDto();
        changeRequest.setNewPassword(NEW_PASSWORD);
        changeRequest.setNewPasswordConfirm(NEW_PASSWORD);

        assertThatThrownBy(() -> userService.changePassword(USER_ID, changeRequest))
                .isInstanceOf(UserNotFoundException.class);
    }
}