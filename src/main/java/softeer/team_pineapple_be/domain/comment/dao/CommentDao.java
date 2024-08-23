package softeer.team_pineapple_be.domain.comment.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import softeer.team_pineapple_be.domain.comment.domain.Comment;
import softeer.team_pineapple_be.domain.comment.response.CommentPageResponse;
import softeer.team_pineapple_be.domain.comment.response.CommentResponse;
import softeer.team_pineapple_be.domain.comment.service.LikeRedisService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static softeer.team_pineapple_be.domain.comment.domain.QComment.comment;

/**
 * comment 최신순 페이징 반환 시 최적화를 위한 Querydsl 사용
 */
@Repository
@RequiredArgsConstructor
public class CommentDao {

    private final JPAQueryFactory queryFactory;

    public CommentPageResponse getCommentsSortedByRecent(int page, LocalDate date, LikeRedisService likeRedisService) {
        int pageSize = 10;

        // 1) 댓글 ID 조회
        List<Long> ids = queryFactory
                .select(comment.id)
                .from(comment)
                .where(comment.postTime.between(date.atStartOfDay(), date.atTime(LocalTime.MAX)))
                .orderBy(comment.postTime.desc())
                .limit(pageSize)
                .offset(page * pageSize)
                .fetch();

        // 2) 총 댓글 수
        Long totalCount = queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.postTime.between(date.atStartOfDay(), date.atTime(LocalTime.MAX)))
                .fetchOne();

        // 3) 댓글 정보 조회
        List<CommentResponse> commentResponseList = List.of();
        if (!ids.isEmpty()) {
            List<Comment> comments = queryFactory
                    .selectFrom(comment)
                    .where(comment.id.in(ids))
                    .orderBy(comment.postTime.desc())
                    .fetch();

            commentResponseList = comments.stream()
                    .map(comment -> CommentResponse.fromComment(comment, likeRedisService))
                    .toList();
        }

        // 4) CommentPageResponse 반환
        Page<CommentResponse> commentPage = new PageImpl<>(commentResponseList, PageRequest.of(page, pageSize), totalCount);
        return CommentPageResponse.fromCommentPage(commentPage);
    }
}