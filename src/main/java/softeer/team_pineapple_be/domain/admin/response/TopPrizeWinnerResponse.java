package softeer.team_pineapple_be.domain.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 1등 응답을 위한 response클래스
 */
@Getter
@AllArgsConstructor
public class TopPrizeWinnerResponse {
    private String phoneNumber;
}
