package softeer.team_pineapple_be.domain.draw.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import softeer.team_pineapple_be.domain.draw.domain.DrawHistory;

import java.time.LocalDateTime;

/**
 * 응모 현황 응답
 */
@Getter
@AllArgsConstructor
public class DrawHistoryResponse {
    private Long id;
    private Byte drawResult;
    private String phoneNumber;
    private LocalDateTime createdDate;

    public static DrawHistoryResponse fromDrawHistory(DrawHistory drawHistory) {
        return new DrawHistoryResponse(
                drawHistory.getId(),
                drawHistory.getDrawResult(),
                drawHistory.getPhoneNumber(),
                drawHistory.getCreateAt()
        );
    }
}
