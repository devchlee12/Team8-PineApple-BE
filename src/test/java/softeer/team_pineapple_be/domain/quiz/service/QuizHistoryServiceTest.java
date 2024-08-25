package softeer.team_pineapple_be.domain.quiz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;
import softeer.team_pineapple_be.domain.admin.repisotory.EventDayInfoRepository;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.domain.quiz.domain.QuizContent;
import softeer.team_pineapple_be.domain.quiz.domain.QuizHistory;
import softeer.team_pineapple_be.domain.quiz.repository.QuizHistoryRepository;
import softeer.team_pineapple_be.domain.quiz.response.QuizHistoryResponse;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizHistoryServiceTest {

    @InjectMocks
    private QuizHistoryService quizHistoryService;

    @Mock
    private QuizHistoryRepository quizHistoryRepository;

    @Mock
    private EventDayInfoRepository eventDayInfoRepository;

    private QuizContent quizContent1;
    private QuizContent quizContent2;
    private QuizContent quizContent3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        quizContent1 = new QuizContent(1, "퀴즈 설명",           // quizDescription
                "첫 번째 질문",       // quizQuestion1
                "두 번째 질문",       // quizQuestion2
                "세 번째 질문",       // quizQuestion3
                "네 번째 질문",       // quizQuestion4
                LocalDate.now().minusDays(2)      // quizDate
        );
        quizContent2 = new QuizContent(2, "퀴즈 설명",           // quizDescription
                "첫 번째 질문",       // quizQuestion1
                "두 번째 질문",       // quizQuestion2
                "세 번째 질문",       // quizQuestion3
                "네 번째 질문",       // quizQuestion4
                LocalDate.now().minusDays(1)      // quizDate
        );
        quizContent3 = new QuizContent(3, "퀴즈 설명",           // quizDescription
                "첫 번째 질문",       // quizQuestion1
                "두 번째 질문",       // quizQuestion2
                "세 번째 질문",       // quizQuestion3
                "네 번째 질문",       // quizQuestion4
                LocalDate.now()     // quizDate
        );
    }

    @Test
    void getDayNRetentionAndDAU_WithNoEventDayInfo_ShouldReturnDefaultValues() {
        // Given
        when(eventDayInfoRepository.findByEventDate(LocalDate.now())).thenReturn(Optional.empty());
        List<QuizHistory> histories = new ArrayList<>();
        when(quizHistoryRepository.findAll()).thenReturn(histories);

        // When
        QuizHistoryResponse response = quizHistoryService.getDayNRetentionAndDAU();

        // Then
        assertEquals(14, response.getDayNRetention().length);
        assertEquals(14, response.getDau().length);
    }

    @Test
    void getDayNRetentionAndDAU_WithEventDayInfo_ShouldCalculateRetentionCorrectly() {
        // Given
        EventDayInfo eventDayInfo = new EventDayInfo(3, LocalDate.now());
        when(eventDayInfoRepository.findByEventDate(LocalDate.now())).thenReturn(Optional.of(eventDayInfo));

        List<QuizHistory> histories = new ArrayList<>();
        Member member1 = new Member("010-1234-5678"); // Assume Member is properly defined
        Member member2 = new Member("010-1234-5679");
        Member member3 = new Member("010-1234-5670");

        // Adding QuizHistory records for two members
        QuizHistory history1 = new QuizHistory(member1, quizContent1);
        QuizHistory history2 = new QuizHistory(member2, quizContent2);
        QuizHistory history3 = new QuizHistory(member1, quizContent2);
        QuizHistory history4 = new QuizHistory(member1, quizContent3);
        histories.add(history1);
        histories.add(history2);
        histories.add(history3);
        histories.add(history4);

        when(quizHistoryRepository.findAll()).thenReturn(histories);

        // When
        QuizHistoryResponse response = quizHistoryService.getDayNRetentionAndDAU();

        // Then
        assertEquals(3, response.getDayNRetention().length);
        assertEquals(3, response.getDau().length);
        assertTrue(response.getDayNRetention()[0][0] > 0); // Ensure some retention calculation has occurred
        assertTrue(response.getDau()[0] > 0);
    }

    @Test
    void getDayNRetentionAndDAU_WithEventDayInfo_ShouldCalculateRetentionCorrectly2() {
        // Given
        EventDayInfo eventDayInfo = new EventDayInfo(3, LocalDate.now());
        when(eventDayInfoRepository.findByEventDate(LocalDate.now())).thenReturn(Optional.of(eventDayInfo));

        List<QuizHistory> histories = new ArrayList<>();
        Member member1 = new Member("010-1234-5678"); // Assume Member is properly defined
        Member member2 = new Member("010-1234-5679");

        // Adding QuizHistory records for two members
        QuizHistory history1 = new QuizHistory(member1, quizContent1);
        QuizHistory history2 = new QuizHistory(member2, quizContent2);
        QuizHistory history3 = new QuizHistory(member1, quizContent2);
        histories.add(history1);
        histories.add(history2);
        histories.add(history3);

        when(quizHistoryRepository.findAll()).thenReturn(histories);

        // When
        QuizHistoryResponse response = quizHistoryService.getDayNRetentionAndDAU();

        // Then
        assertEquals(3, response.getDayNRetention().length);
        assertEquals(3, response.getDau().length);
        assertTrue(response.getDayNRetention()[0][0] > 0); // Ensure some retention calculation has occurred
        assertTrue(response.getDau()[0] > 0);
    }

    // 추가적인 테스트 케이스를 작성하여 각 메소드의 경계 조건이나 다양한 시나리오를 검증할 수 있습니다.
}
