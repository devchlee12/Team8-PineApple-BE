package softeer.team_pineapple_be.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;
import softeer.team_pineapple_be.domain.admin.exception.AdminErrorCode;
import softeer.team_pineapple_be.domain.admin.repisotory.EventDayInfoRepository;
import softeer.team_pineapple_be.domain.admin.response.TopPrizeWinnerResponse;
import softeer.team_pineapple_be.domain.draw.domain.DrawHistory;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.draw.repository.DrawHistoryRepository;
import softeer.team_pineapple_be.domain.draw.repository.DrawRewardInfoRepository;
import softeer.team_pineapple_be.global.common.utils.RandomUtils;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.time.LocalDate;

/**
 * 1등 추첨을 위한 클래스
 */
@Service
@RequiredArgsConstructor
public class TopPrizeWinnerService {

    private final EventDayInfoRepository eventDayInfoRepository;
    private final DrawHistoryRepository drawHistoryRepository;
    private final DrawRewardInfoRepository drawRewardInfoRepository;

    private static final int SCHEDULE_LENGTH = 14;
    private static final byte TOP_PRIZE_REWARD_ID = 1;

    /**
     * 1등을 추첨하는 메서드
     * @return 1등 당첨된 유저에 대한 핸드폰 번호를 담고있는 객체
     */
    @Transactional
    public TopPrizeWinnerResponse getTopPrizeWinner() {
        validateDrawDate();

        long totalCount = drawHistoryRepository.count();
        Long topPrizeIndex = RandomUtils.getPositiveNumber(totalCount);

        DrawHistory drawHistory = findDrawHistoryById(topPrizeIndex);
        DrawRewardInfo drawRewardInfo = findDrawRewardInfoById(TOP_PRIZE_REWARD_ID);

        processReward(drawRewardInfo);
        saveTopPrizeDrawHistory(drawHistory);

        return new TopPrizeWinnerResponse(drawHistory.getPhoneNumber());
    }

    private void validateDrawDate() {
        EventDayInfo eventDayInfo = eventDayInfoRepository.findById(SCHEDULE_LENGTH)
                .orElseThrow(() -> new RestApiException(AdminErrorCode.NOT_EVENT_DAY));

        LocalDate drawDate = LocalDate.now();
        if (!drawDate.isAfter(eventDayInfo.getEventDate())) {
            throw new RestApiException(AdminErrorCode.CAN_NOT_DRAW_TOP_PRIZE_WINNER);
        }
    }

    private DrawHistory findDrawHistoryById(Long id) {
        return drawHistoryRepository.findById(id)
                .orElseThrow(() -> new RestApiException(DrawErrorCode.NOT_VALID_WINNER));
    }

    private DrawRewardInfo findDrawRewardInfoById(byte id) {
        return drawRewardInfoRepository.findById(id)
                .orElseThrow(() -> new RestApiException(DrawErrorCode.NO_PRIZE));
    }

    private void processReward(DrawRewardInfo drawRewardInfo) {
        if (drawRewardInfo.getStock() <= 0) {
            throw new RestApiException(DrawErrorCode.NO_PRIZE);
        }
        drawRewardInfo.decreaseStock();
        drawRewardInfoRepository.save(drawRewardInfo);
    }

    private void saveTopPrizeDrawHistory(DrawHistory drawHistory) {
        DrawHistory topPrizeDrawHistory = new DrawHistory(TOP_PRIZE_REWARD_ID, drawHistory.getPhoneNumber());
        drawHistoryRepository.save(topPrizeDrawHistory);
    }
}
