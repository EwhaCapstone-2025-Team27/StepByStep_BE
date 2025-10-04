package com.dragon.stepbystep.repository;

import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.GenderType;
import com.dragon.stepbystep.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNicknameAndGenderAndBirthyearAndStatus(
            String nickname, GenderType gender, Integer birthyear, UserStatus status
    );

    Optional<User> findByNicknameAndGenderAndBirthyear(
            String nickname, GenderType gender, Integer birthyear
    );

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Long id(Long id);
}