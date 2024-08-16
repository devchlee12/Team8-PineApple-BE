package softeer.team_pineapple_be.domain.draw.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;

/**
 * 경품 이미지 응답
 */
@Getter
@Setter
@AllArgsConstructor
public class DrawRewardImageResponse {
  private Byte rank;
  private String imageUrl;

  public static DrawRewardImageResponse of(DrawRewardInfo drawRewardInfo) {
    return new DrawRewardImageResponse(drawRewardInfo.getRanking(), drawRewardInfo.getImage());
  }
}
