package softeer.team_pineapple_be.domain.draw.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * 모든 랭킹과 확률을 맵 형태로 반환하는 Response 클래스
 */
@Getter
@AllArgsConstructor
public class DrawProbabilityResponse {
    private Map<Byte, Integer> probabilities;
}