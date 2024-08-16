package softeer.team_pineapple_be.domain.draw.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 당첨 확률 수정 요청을 위한 Request 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DrawProbabilityRequest {
    private Map<Byte, Integer> probabilities; // 랭킹과 확률을 포함하는 맵
}