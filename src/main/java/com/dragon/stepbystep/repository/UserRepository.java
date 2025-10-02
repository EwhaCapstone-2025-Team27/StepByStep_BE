package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.GenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByNickname(String nickname);
    boolean existsByNickname(String nickname);

    // 이메일 찾기(닉네임+성별+출생년도)
    Optional<User> findByNicknameAndGenderAndBirthyear(String nickname, GenderType gender, Integer birthyear);
}