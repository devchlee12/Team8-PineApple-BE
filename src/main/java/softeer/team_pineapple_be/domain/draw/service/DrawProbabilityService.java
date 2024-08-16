package softeer.team_pineapple_be.domain.draw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softeer.team_pineapple_be.domain.draw.domain.DrawProbability;
import softeer.team_pineapple_be.domain.draw.repository.DrawProbabilityRepository;
import softeer.team_pineapple_be.domain.draw.request.DrawProbabilityRequest;
import softeer.team_pineapple_be.domain.draw.response.DrawProbabilityResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 경품 확률 서비스
 */
@Service
@RequiredArgsConstructor
public class DrawProbabilityService {

    private final DrawProbabilityRepository drawProbabilityRepository;

    /**
     * 경품 확률을 조회하는 메서드
     * @return 경품 확률 리스트
     */
    @Transactional(readOnly = true)
    public DrawProbabilityResponse getDrawProbability(){
        List<DrawProbability> probabilities = drawProbabilityRepository.findAll();
        Map<Byte, Integer> probabilitiesMap = probabilities.stream()
                .collect(Collectors.toMap(DrawProbability::getRanking, DrawProbability::getProbability));

        return new DrawProbabilityResponse(probabilitiesMap);
    }

    /**
     * 경품 확률을 수정하는 메서드
     * @param request 수정하고자 하는 경품 확률
     */
    @Transactional
    public void setDrawProbability(DrawProbabilityRequest request) {
        Map<Byte, Integer> probabilities = request.getProbabilities();

        List<DrawProbability> drawProbabilities = probabilities.entrySet().stream()
                .map(entry -> new DrawProbability(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        drawProbabilityRepository.saveAll(drawProbabilities);
    }
}
