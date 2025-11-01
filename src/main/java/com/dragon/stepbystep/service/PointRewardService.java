package com.dragon.stepbystep.service;

import com.dragon.stepbystep.domain.PointHistory;
import com.dragon.stepbystep.domain.User;
import com.dragon.stepbystep.domain.enums.PointHistoryType;
import com.dragon.stepbystep.repository.PointHistoryRepository;
import com.dragon.stepbystep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointRewardService {

    public static final int BASE_REWARD_POINT = 5;

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void rewardForBoardPost(Long userId) {
        reward(userId, BASE_REWARD_POINT, "게시글 작성 보상");
    }

    @Transactional
    public void rewardForChatQuestion(Long userId) {
        reward(userId, BASE_REWARD_POINT, "챗봇 질문 보상");
    }

    @Transactional
    public void rewardForQuizCorrectAnswers(Long userId, int correctCount) {
        if (correctCount <= 0) {
            return;
        }
        int rewardPoints = correctCount * BASE_REWARD_POINT;
        reward(userId, rewardPoints, String.format("퀴즈 정답 %d개 보상", correctCount));
    }

    private void reward(Long userId, int points, String title) {
        if (userId == null || points <= 0) {
            return;
        }
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.warn("포인트 적립 실패: 사용자({})를 찾을 수 없습니다.", userId);
            return;
        }

        User user = optionalUser.get();
        user.increasePoints(points);

        pointHistoryRepository.save(
                PointHistory.builder()
                        .user(user)
                        .type(PointHistoryType.EARN)
                        .title(title)
                        .pointChange(points)
                        .balanceAfter(user.getPoints())
                        .build()
        );
    }
}