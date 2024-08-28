package softeer.team_pineapple_be.domain.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import softeer.team_pineapple_be.domain.comment.dao.CommentDao;
import softeer.team_pineapple_be.domain.comment.domain.Comment;
import softeer.team_pineapple_be.domain.comment.domain.CommentLike;
import softeer.team_pineapple_be.domain.comment.domain.id.LikeId;
import softeer.team_pineapple_be.domain.comment.exception.CommentErrorCode;
import softeer.team_pineapple_be.domain.comment.repository.CommentLikeRepository;
import softeer.team_pineapple_be.domain.comment.repository.CommentRepository;
import softeer.team_pineapple_be.domain.comment.request.CommentLikeRequest;
import softeer.team_pineapple_be.domain.comment.request.CommentRequest;
import softeer.team_pineapple_be.domain.comment.response.CommentPageResponse;
import softeer.team_pineapple_be.domain.comment.response.CommentResponse;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.domain.member.exception.MemberErrorCode;
import softeer.team_pineapple_be.domain.member.repository.MemberRepository;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.lock.annotation.DistributedLock;

/**
 * 기대평 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final AuthMemberService authMemberService;
  private final MemberRepository memberRepository;
  private final LikeRedisService likeRedisService;
  private final CommentDao commentDao;

  /**
   * 기대평 ID로 기대평 가져오기
   *
   * @param id
   * @return
   */
  @Transactional
  public CommentResponse getCommentById(Long id) {
    Comment comment =
        commentRepository.findById(id).orElseThrow(() -> new RestApiException(CommentErrorCode.NO_COMMENT));
    return CommentResponse.fromComment(comment, likeRedisService);
  }

  /**
   * 기대평을 좋아요 순으로 가져오는 메서드
   *
   * @param page
   * @return 좋아요 순 정렬 기대평 목록
   */
  public CommentPageResponse getCommentsSortedByLikes(int page, LocalDate date) {
    PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "likeCount"));
    Page<Comment> commentPage =
        commentRepository.findAllByPostTimeBetween(pageRequest, date.atStartOfDay(), date.atTime(LocalTime.MAX));
    return CommentPageResponse.fromCommentPage(commentPage, likeRedisService);
  }

  /**
   * 기대평을 최신순으로 가져오는 메서드
   *
   * @param page
   * @return 최신순 정렬 기대평 목록
   */
  public CommentPageResponse getCommentsSortedByRecent(int page, LocalDate date) {
    return commentDao.getCommentsSortedByRecent(page, date, likeRedisService);
  }

  /**
   * 당일 가장 많은 좋아요를 받은 사람에게 10개의 툴박스 주기
   */
  @Scheduled(cron = "0 0 0 * * *")
  @DistributedLock(key = "topTen")
  public void giveTenToolBoxToTopTenComment() {
    if (likeRedisService.isTopTenBatched()) {
      return;
    }
    LocalDate yesterday = LocalDate.now().minusDays(1);
    List<Comment> topComments =
        commentRepository.findTop10CommentsByPostTimeBetweenOrderByLikeCountDescIdAsc(yesterday.atStartOfDay(),
            yesterday.atTime(LocalTime.MAX));


    List<Member> updatedMembers = new ArrayList<>();

    for (Comment comment : topComments) {
      String phoneNumber = comment.getPhoneNumber();
      Member member = memberRepository.findById(phoneNumber)
                                      .orElseThrow(() -> new RestApiException(MemberErrorCode.NO_MEMBER)); // 유저 찾기

      member.increment10ToolBoxCnt();

      updatedMembers.add(member); // 변경된 멤버를 리스트에 추가

    }


    memberRepository.saveAll(updatedMembers);

  }

  /**
   * 기대평 작성 하는 메서드
   *
   * @param commentRequest
   */
  @DistributedLock(key = "#memberPhoneNumber")
  public void saveComment(String memberPhoneNumber, CommentRequest commentRequest) {
    if (wasMemberCommentedToday(memberPhoneNumber)) {
      throw new RestApiException(CommentErrorCode.ALREADY_REVIEWED);
    }
    Member member =
        memberRepository.findById(memberPhoneNumber).orElseThrow(() -> new RestApiException(MemberErrorCode.NO_MEMBER));
    member.incrementToolBoxCnt();
    commentRepository.save(new Comment(commentRequest.getContent(), memberPhoneNumber));
  }

  /**
   * 좋아요 누름 처리하는 메서드 이미 눌렀으면 좋아요를 줄이고, 안눌렀으면 좋아요 증가시킴
   *
   * @param commentLikeRequest
   */
  @Transactional
  @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 10,
      backoff = @Backoff(random = true, delay = 500, maxDelay = 1000))
  public void saveCommentLike(String memberPhoneNumber, CommentLikeRequest commentLikeRequest) {
    LikeId likeId = new LikeId(commentLikeRequest.getCommentId(), memberPhoneNumber);
    Optional<CommentLike> byId = commentLikeRepository.findById(likeId);

    if (byId.isPresent()) {
      decreaseCommentLikeCount(commentLikeRequest.getCommentId());
      likeRedisService.removeLike(commentLikeRequest.getCommentId());
      commentLikeRepository.delete(byId.get());
      return;
    }
    increaseCommentLikeCount(commentLikeRequest.getCommentId());
    likeRedisService.addLike(commentLikeRequest.getCommentId());
    commentLikeRepository.save(new CommentLike(likeId));
  }

  /**
   * 좋아요 감소
   *
   * @param commentId
   */
  private void decreaseCommentLikeCount(Long commentId) {
    Comment comment =
        commentRepository.findById(commentId).orElseThrow(() -> new RestApiException(CommentErrorCode.NO_COMMENT));
    comment.decreaseLikeCount();
  }

  /**
   * 좋아요 증가
   *
   * @param commentId
   */
  private void increaseCommentLikeCount(Long commentId) {
    Comment comment =
        commentRepository.findById(commentId).orElseThrow(() -> new RestApiException(CommentErrorCode.NO_COMMENT));
    comment.increaseLikeCount();
  }

  /**
   * 오늘 이미 기대평 작성했는지 확인하는 메서드
   *
   * @param memberPhoneNumber
   * @return 오늘 기대평 작성 여부
   */
  private boolean wasMemberCommentedToday(String memberPhoneNumber) {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
    Optional<Comment> commentsByAuthorAndDate =
        commentRepository.findByPhoneNumberAndPostTimeBetween(memberPhoneNumber, startOfDay, endOfDay);
    return commentsByAuthorAndDate.isPresent();
  }
}
