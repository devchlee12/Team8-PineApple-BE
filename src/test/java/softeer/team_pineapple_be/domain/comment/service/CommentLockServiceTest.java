package softeer.team_pineapple_be.domain.comment.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import softeer.team_pineapple_be.domain.comment.domain.Comment;
import softeer.team_pineapple_be.domain.comment.exception.CommentErrorCode;
import softeer.team_pineapple_be.domain.comment.repository.CommentRepository;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.util.Optional;

public class CommentLockServiceTest {

    @InjectMocks
    private CommentLockService commentLockService; // 테스트할 서비스 클래스

    @Mock
    private CommentRepository commentRepository; // 댓글 리포지토리 Mock

    @Mock
    private Comment comment; // 댓글 Mock

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIncreaseCommentLikeCountWithLock_Success() {
        // Given
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment)); // 댓글 Mock 설정

        // When
        commentLockService.increaseCommentLikeCountWithLock(commentId);

        // Then
        verify(comment).increaseLikeCount(); // increaseLikeCount 메서드가 호출되었는지 검증
        verify(commentRepository).findById(commentId); // findById 메서드 호출 검증
    }

    @Test
    public void testIncreaseCommentLikeCountWithLock_NoCommentFound() {
        // Given
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty()); // 댓글 없음 설정

        // When & Then
        assertThatThrownBy(() -> commentLockService.increaseCommentLikeCountWithLock(commentId)).isInstanceOf(
                RestApiException.class).satisfies(exception -> {
            RestApiException restApiException = (RestApiException) exception;
            assertThat(restApiException.getErrorCode()).isEqualTo(CommentErrorCode.NO_COMMENT);
        });
    }
}
