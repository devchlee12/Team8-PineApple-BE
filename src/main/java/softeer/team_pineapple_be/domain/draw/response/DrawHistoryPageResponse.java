package softeer.team_pineapple_be.domain.draw.response;

import org.springframework.data.domain.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import softeer.team_pineapple_be.domain.draw.domain.DrawHistory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 응모 현황 목록 응답
 */
@Getter
@AllArgsConstructor
public class DrawHistoryPageResponse {
    private Integer totalPages;
    private Long totalItems;
    private List<DrawHistoryResponse> drawHistories;

    public static DrawHistoryPageResponse fromDrawHistoryPage(Page<DrawHistory> drawHistoriesPage) {
        List<DrawHistoryResponse> drawHistoryResponseList = drawHistoriesPage.getContent().stream()
                .map(DrawHistoryResponse::fromDrawHistory)
                .collect(Collectors.toList());
        return new DrawHistoryPageResponse(drawHistoriesPage.getTotalPages(), drawHistoriesPage.getTotalElements(), drawHistoryResponseList);
    }
}
