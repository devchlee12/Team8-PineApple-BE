package softeer.team_pineapple_be.domain.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import softeer.team_pineapple_be.domain.comment.domain.Comment;

/**
 * 기대평 리포지토리
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
  Page<Comment> findAllByPostTimeBetween(Pageable pageable, LocalDateTime startOfDay, LocalDateTime endOfDay);

  Optional<Comment> findByPhoneNumberAndPostTimeBetween(@Param("phoneNumber") String phoneNumber,
      @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

  List<Comment> findTop10CommentsByPostTimeBetweenOrderByLikeCountDescIdAsc(@Param("startOfDay") LocalDateTime startOfDay,
                                             @Param("endOfDay") LocalDateTime endOfDay);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Comment> findById(Long aLong);
}
