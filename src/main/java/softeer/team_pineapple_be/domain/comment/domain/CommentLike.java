package softeer.team_pineapple_be.domain.comment.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import softeer.team_pineapple_be.domain.comment.domain.id.LikeId;

/**
 * 기대평 좋아요 이력
 */
@Entity
@Getter
@NoArgsConstructor
public class CommentLike {
  @EmbeddedId
  private LikeId id;
  @Version
  private Long version;

  public CommentLike(LikeId id) {
    this.id = id;
  }
}
