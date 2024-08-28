package softeer.team_pineapple_be.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import softeer.team_pineapple_be.domain.comment.domain.CommentLike;
import softeer.team_pineapple_be.domain.comment.domain.id.LikeId;

/**
 * 좋아요 리포지토리
 */
public interface CommentLikeRepository extends JpaRepository<CommentLike, LikeId> {
  @Lock(LockModeType.OPTIMISTIC)
  Optional<CommentLike> findById(LikeId likeId);
}
