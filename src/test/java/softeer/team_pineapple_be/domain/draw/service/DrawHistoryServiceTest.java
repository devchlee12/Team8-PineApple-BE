package softeer.team_pineapple_be.domain.draw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import softeer.team_pineapple_be.domain.draw.domain.DrawHistory;
import softeer.team_pineapple_be.domain.draw.repository.DrawHistoryRepository;
import softeer.team_pineapple_be.domain.draw.response.DrawHistoryPageResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DrawHistoryServiceTest {

    @Mock
    private DrawHistoryRepository drawHistoryRepository;

    @InjectMocks
    private DrawHistoryService drawHistoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDrawHistory() {
        // given
        int page = 0;
        int limit = 10;
        String sort = "desc";

        DrawHistory drawHistory1 = new DrawHistory((byte)1 , "010-1234-5678");
        DrawHistory drawHistory2 = new DrawHistory((byte)2 , "010-1234-5679");
        List<DrawHistory> drawHistories = List.of(drawHistory1, drawHistory2);
        Page<DrawHistory> drawHistoryPage = new PageImpl<>(drawHistories, PageRequest.of(page, limit), drawHistories.size());

        when(drawHistoryRepository.findAll(any(Pageable.class))).thenReturn(drawHistoryPage);

        // when
        DrawHistoryPageResponse response = drawHistoryService.getDrawHistory(page, limit, sort);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getDrawHistories()).hasSize(2); // 예제에서는 2개의 항목이 있다고 가정
        assertThat(response.getTotalItems()).isEqualTo(2); // 총 항목 수 확인
        verify(drawHistoryRepository, times(1)).findAll(any(Pageable.class)); // 메서드가 호출되었는지 확인
    }
}
