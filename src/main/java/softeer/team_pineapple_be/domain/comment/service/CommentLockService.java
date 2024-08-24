package softeer.team_pineapple_be.domain.comment.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.comment.domain.Comment;
import softeer.team_pineapple_be.domain.comment.exception.CommentErrorCode;
import softeer.team_pineapple_be.domain.comment.repository.CommentRepository;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.lock.annotation.DistributedLock;

/**
 * 기대평 락 서비스
 */
@RequiredArgsConstructor
@Service
public class CommentLockService {
  private final CommentRepository commentRepository;

  @DistributedLock(key = "#commentId")
  public void increaseCommentLikeCountWithLock(Long commentId) {
    Comment comment =
        commentRepository.findById(commentId).orElseThrow(() -> new RestApiException(CommentErrorCode.NO_COMMENT));
    comment.increaseLikeCount();
  }
}
