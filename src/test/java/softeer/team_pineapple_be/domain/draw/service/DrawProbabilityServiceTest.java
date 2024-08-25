package softeer.team_pineapple_be.domain.draw.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import softeer.team_pineapple_be.domain.draw.domain.DrawProbability;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.draw.repository.DrawProbabilityRepository;
import softeer.team_pineapple_be.domain.draw.request.DrawProbabilityRequest;
import softeer.team_pineapple_be.domain.draw.response.DrawProbabilityResponse;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DrawProbabilityServiceTest {

    @Mock
    private DrawProbabilityRepository drawProbabilityRepository;

    @InjectMocks
    private DrawProbabilityService drawProbabilityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("응모 확률을 조회했으나 찾아지지 않는 경우 - FailureCase")
    void testGetDrawProbabilityByRanking_NotFound() {
        // given
        Byte ranking = 5;
        when(drawProbabilityRepository.findById(ranking)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> drawProbabilityService.getDrawProbabilityByRanking(ranking))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(DrawErrorCode.NO_PRIZE_PROBABILITY);
                });
    }
    @Test
    @DisplayName("응모 확률 조회에 성공한 경우 - SuccessCase")
    void testGetDrawProbability() {
        // given
        DrawProbability drawProbability1 = new DrawProbability((byte) 1, 10);
        DrawProbability drawProbability2 = new DrawProbability((byte) 2, 20);
        List<DrawProbability> probabilities = List.of(drawProbability1, drawProbability2);

        when(drawProbabilityRepository.findAll()).thenReturn(probabilities);

        // when
        DrawProbabilityResponse response = drawProbabilityService.getDrawProbability();

        // then
        assertThat(response).isNotNull();
        Map<Byte, Integer> probabilitiesMap = response.getProbabilities();
        assertThat(probabilitiesMap).hasSize(2);
        assertThat(probabilitiesMap.get((byte) 1)).isEqualTo(10);
        assertThat(probabilitiesMap.get((byte) 2)).isEqualTo(20);
    }

    @Test
    @DisplayName("캐시에서 조회가 성공한 경우 - SuccessCase")
    void testGetDrawProbabilityByRanking_CacheHit() {
        // given
        Byte ranking = 1;
        Integer probabilityValue = 10;

        // Mocking the behavior to simulate cache hit
        when(drawProbabilityRepository.findById(ranking)).thenReturn(Optional.of(new DrawProbability(ranking, probabilityValue)));

        // when
        Integer probability = drawProbabilityService.getDrawProbabilityByRanking(ranking);

        // then
        assertThat(probability).isEqualTo(probabilityValue);
    }

    @Test
    @DisplayName("캐시에서 조회가 실패한 경우 - FailureCase")
    void testGetDrawProbabilityByRanking_CacheMiss() {
        // given
        Byte ranking = 1;
        Integer probabilityValue = 10;
        DrawProbability drawProbability = new DrawProbability(ranking, probabilityValue);

        when(drawProbabilityRepository.findById(ranking)).thenReturn(Optional.of(drawProbability));

        // when
        Integer probability = drawProbabilityService.getDrawProbabilityByRanking(ranking);

        // then
        assertThat(probability).isEqualTo(probabilityValue);
        verify(drawProbabilityRepository, times(1)).findById(ranking); // Repository 호출이 있어야 함
    }


    @Test
    @DisplayName("성공적으로 당첨 확률을 수정한 경우 - SuccessCase")
    void testSetDrawProbability() {
        // given
        DrawProbabilityRequest request = new DrawProbabilityRequest(Map.of((byte) 1, 10, (byte) 2, 20));

        // when
        drawProbabilityService.setDrawProbability(request);

        // then
        verify(drawProbabilityRepository, times(1)).saveAll(anyList()); // saveAll이 호출되어야 함
    }
}
