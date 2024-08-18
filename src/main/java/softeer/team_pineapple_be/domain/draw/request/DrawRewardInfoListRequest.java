package softeer.team_pineapple_be.domain.draw.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 당첨 상품 수정을 위한 Request 클래스
 */
@Getter
@Setter
@NoArgsConstructor
public class DrawRewardInfoListRequest {
    private List<DrawRewardInfoRequest> rewards;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DrawRewardInfoRequest {
        private Byte ranking;
        private String name;
        private Integer stock;
        private MultipartFile image;

    }
}
