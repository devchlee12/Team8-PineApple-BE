package softeer.team_pineapple_be.domain.draw.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;
import softeer.team_pineapple_be.domain.draw.service.DrawProbabilityService;

/**
 * 경품 이미지 응답
 */
@Getter
@Setter
@AllArgsConstructor
public class DrawRewardInfoResponse {
  private Byte rank;
  private String rewardName;
  private Integer rewardCount;
  private String imageUrl;

  public static DrawRewardInfoResponse of(DrawRewardInfo drawRewardInfo,
      DrawProbabilityService drawProbabilityService) {
    return new DrawRewardInfoResponse(drawRewardInfo.getRanking(), drawRewardInfo.getName(),
        drawProbabilityService.getDrawProbabilityByRanking(drawRewardInfo.getRanking()), drawRewardInfo.getImage());
  }
}
