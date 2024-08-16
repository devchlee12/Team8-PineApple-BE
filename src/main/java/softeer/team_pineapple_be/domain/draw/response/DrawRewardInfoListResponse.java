package softeer.team_pineapple_be.domain.draw.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 당첨 상품 리스트 조회를 위한 Response 클래스
 */
@Getter
@AllArgsConstructor
public class DrawRewardInfoListResponse {
    private List<DrawRewardInfoResponse> rewards;

    @Getter
    @AllArgsConstructor
    public static class DrawRewardInfoResponse {
        private Byte ranking;
        private String name;
        private Integer stock;
        private String image;
    }
}

