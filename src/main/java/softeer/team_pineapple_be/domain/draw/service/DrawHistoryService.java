package softeer.team_pineapple_be.domain.draw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softeer.team_pineapple_be.domain.draw.domain.DrawHistory;
import softeer.team_pineapple_be.domain.draw.repository.DrawHistoryRepository;
import softeer.team_pineapple_be.domain.draw.response.DrawHistoryPageResponse;

/**
 * 응모 기록에 대한 처리를 담당하는 클래스
 */
@Service
@RequiredArgsConstructor
public class DrawHistoryService {

    private final DrawHistoryRepository drawHistoryRepository;

    /**
     * 응모 현황을 최신순으로 가져오는 메서드
     *
     * @param page 페이지 번호
     * @param limit 페이지당 항목 수
     * @return 최신순 정렬 응모 현황 목록
     */
    @Transactional(readOnly = true)
    public DrawHistoryPageResponse getDrawHistory(int page, int limit, String sort) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.fromString(sort.toUpperCase()), "createAt"));
        Page<DrawHistory> drawHistoryPage = drawHistoryRepository.findAll(pageable);
        return DrawHistoryPageResponse.fromDrawHistoryPage(drawHistoryPage);
    }
}
