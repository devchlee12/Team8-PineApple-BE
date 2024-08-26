package softeer.team_pineapple_be.domain.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommentServiceTest {

  @InjectMocks
  private CommentService commentService;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentLikeRepository commentLikeRepository;

  @Mock
  private AuthMemberService authMemberService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private CommentDao commentDao;

  @Mock
  private LikeRedisService likeRedisService;

  private String phoneNumber = "010-1234-5678";
  private List<Comment> comments;

  @Test
  @DisplayName("getCommentsSortedByLikes: 좋아요 순으로 기대평을 가져온다.")
  void getCommentsSortedByLikes() {
    // Given
    LocalDate date = LocalDate.now();
    comments.sort((c1, c2) -> c2.getLikeCount().compareTo(c1.getLikeCount()));
    Page<Comment> mockPage = new PageImpl<>(comments);
    when(commentRepository.findAllByPostTimeBetween(any(), any(), any())).thenReturn(mockPage);

    // When
    CommentPageResponse response = commentService.getCommentsSortedByLikes(0, date);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getComments()).hasSize(5);
    assertThat(response.getComments().get(0).getLikeCount()).isEqualTo(5); // 가장 높은 좋아요 수 확인
  }

  @Test
  @DisplayName("getCommentsSortedByRecent: 최신순으로 기대평을 가져온다.")
  void getCommentsSortedByRecent() {
    // Given
    LocalDate date = LocalDate.now();
    Page<Comment> mockPage = new PageImpl<>(comments);

    // CommentResponse 리스트로 변환
    List<CommentResponse> commentResponseList = comments.stream()
                                                        .map(comment -> CommentResponse.fromComment(comment,
                                                            likeRedisService)) // LikeRedisService는 필요에 따라 조정
                                                        .toList();

    // CommentPageResponse 생성
    CommentPageResponse mockResponse = new CommentPageResponse(mockPage.getTotalPages(), commentResponseList);

    // Mocking commentDao의 메서드
    when(commentDao.getCommentsSortedByRecent(anyInt(), any(LocalDate.class), any(LikeRedisService.class))).thenReturn(
        mockResponse);

    // When
    CommentPageResponse response = commentService.getCommentsSortedByRecent(0, date);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getComments()).hasSize(5);
    assertThat(response.getComments().get(0).getId()).isEqualTo(1); // 가장 최근 댓글 확인
  }

  @Test
  @DisplayName("saveCommentLike: 좋아요를 성공적으로 추가한다.")
  void saveCommentLike_AddLike() {
    // Given
    CommentLikeRequest commentLikeRequest = new CommentLikeRequest(1L);
    Comment comment = new Comment("테스트 내용", phoneNumber);
    when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
    when(commentLikeRepository.findById(any(LikeId.class))).thenReturn(Optional.empty());

    // When
    commentService.saveCommentLike(phoneNumber, commentLikeRequest);

    // Then
    verify(commentLikeRepository).save(any(CommentLike.class));
    assertThat(commentLikeRequest.getCommentId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("saveCommentLike: 좋아요를 취소한다.")
  void saveCommentLike_RemoveLike() {
    // Given
    CommentLikeRequest commentLikeRequest = new CommentLikeRequest(1L);
    Comment comment = new Comment("테스트 내용", phoneNumber);
    comment.increaseLikeCount(); // 초기 좋아요 수 증가
    LikeId likeId = new LikeId(1L, phoneNumber);
    when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
    when(commentLikeRepository.findById(likeId)).thenReturn(Optional.of(new CommentLike(likeId)));

    // When
    commentService.saveCommentLike(phoneNumber, commentLikeRequest);

    // Then
    verify(commentLikeRepository).delete(any(CommentLike.class));
    assertThat(comment.getLikeCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("saveComment: 오늘 이미 기대평을 작성한 경우 예외가 발생한다.")
  void saveComment_AlreadyReviewed() {
    // Given
    CommentRequest commentRequest = new CommentRequest("좋은 제품입니다.");
    when(memberRepository.findById(phoneNumber)).thenReturn(Optional.of(new Member(phoneNumber)));
    when(commentRepository.findByPhoneNumberAndPostTimeBetween(eq(phoneNumber), any(), any())).thenReturn(
        Optional.of(new Comment()));

    // When & Then
    assertThatThrownBy(() -> commentService.saveComment(phoneNumber, commentRequest)).isInstanceOf(
        RestApiException.class).satisfies(exception -> {
      RestApiException restApiException = (RestApiException) exception;
      assertThat(restApiException.getErrorCode()).isEqualTo(CommentErrorCode.ALREADY_REVIEWED);
    });
  }

  @Test
  @DisplayName("saveComment: 성공적으로 기대평을 저장한다.")
  void saveComment_MemberNotExists() {
    // Given
    CommentRequest commentRequest = new CommentRequest("좋은 제품입니다.");
    when(memberRepository.findById(phoneNumber)).thenReturn(Optional.empty());
    when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());

    // When & Then
    assertThatThrownBy(() -> commentService.saveComment(phoneNumber, commentRequest)).isInstanceOf(
        RestApiException.class).satisfies(exception -> {
      RestApiException restApiException = (RestApiException) exception;
      assertThat(restApiException.getErrorCode()).isEqualTo(MemberErrorCode.NO_MEMBER);
    });
  }

  @Test
  @DisplayName("saveComment: 성공적으로 기대평을 저장한다.")
  void saveComment_Success() {
    // Given
    CommentRequest commentRequest = new CommentRequest("좋은 제품입니다.");
    Member member = new Member(phoneNumber);
    when(memberRepository.findById(phoneNumber)).thenReturn(Optional.of(member));
    when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());

    // When
    commentService.saveComment(phoneNumber, commentRequest);

    // Then
    verify(memberRepository).findById(phoneNumber);
    verify(commentRepository).save(any(Comment.class));
  }

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    comments = new ArrayList<>();
    when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
    for (int i = 1; i <= 5; i++) {
      Comment comment = new Comment((long) i, phoneNumber, i + phoneNumber, i, LocalDateTime.now().plusMinutes(i));
      comments.add(comment);
    }

  }

  @Test
  void testGetCommentById_NoCommentFound() {
    // Given
    Long commentId = 1L;
    Comment comment1 = new Comment("댓글1", "010-1234-5678"); // 댓글 Mock
    when(commentRepository.findById(commentId)).thenReturn(Optional.empty()); // 댓글 없음 설정

    // When & Then
    assertThatThrownBy(() -> commentService.getCommentById(commentId)).isInstanceOf(RestApiException.class)
                                                                      .satisfies(exception -> {
                                                                        RestApiException restApiException =
                                                                            (RestApiException) exception;
                                                                        assertThat(
                                                                            restApiException.getErrorCode()).isEqualTo(
                                                                            CommentErrorCode.NO_COMMENT);
                                                                      }); // 예외 메시지 확인
  }

  @Test
  void testGetCommentById_Success() {
    // Given
    Long commentId = 1L;
    Comment comment1 = new Comment("댓글1", "010-1234-5678"); // 댓글 Mock
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment1)); // 댓글 Mock 설정

    // When
    CommentResponse commentResponse = commentService.getCommentById(commentId);

    // Then
    assertThat(commentResponse).isNotNull(); // 응답이 null이 아님을 확인
    assertThat(commentResponse).isInstanceOf(CommentResponse.class); // 응답 타입 확인
  }

  @Test
  void testGiveTenToolBoxToTopTenComment() {
    // Given
    LocalDate yesterday = LocalDate.now().minusDays(1);
    Comment comment1 = new Comment("댓글1", "010-1234-5678"); // 댓글 Mock
    Comment comment2 = new Comment("댓글2", "010-9876-5432");
    Member member1 = new Member("010-1234-5678");
    Member member2 = new Member("010-9876-5432");
    List<Comment> topComments = Arrays.asList(comment1, comment2);

    when(commentRepository.findTop10CommentsByPostTimeBetweenOrderByLikeCountDescIdAsc(yesterday.atStartOfDay(),
        yesterday.atTime(LocalTime.MAX))).thenReturn(topComments); // 댓글 Mock 설정

    when(memberRepository.findById(comment1.getPhoneNumber())).thenReturn(Optional.of(member1));
    when(memberRepository.findById(comment2.getPhoneNumber())).thenReturn(Optional.of(member2));

    // When
    commentService.giveTenToolBoxToTopTenComment();

    // Then
    verify(memberRepository).saveAll(anyList()); // saveAll 메서드 호출 확인
    assertThat(member1.getToolBoxCnt()).isEqualTo(10); // 툴박스 수량 확인 (상태에 따라 조정 필요)
  }

  @Test
  void testGiveTenToolBoxToTopTenComment_NoMemberFound() {
    // Given
    LocalDate yesterday = LocalDate.now().minusDays(1);
    Comment comment1 = new Comment("댓글1", "010-1234-5678"); // 댓글 Mock
    List<Comment> topComments = Arrays.asList(comment1);

    when(commentRepository.findTop10CommentsByPostTimeBetweenOrderByLikeCountDescIdAsc(yesterday.atStartOfDay(),
        yesterday.atTime(LocalTime.MAX))).thenReturn(topComments); // 댓글 Mock 설정

    when(memberRepository.findById(comment1.getPhoneNumber())).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> commentService.giveTenToolBoxToTopTenComment()).isInstanceOf(RestApiException.class)
                                                                            .satisfies(exception -> {
                                                                              RestApiException restApiException =
                                                                                  (RestApiException) exception;
                                                                              assertThat(
                                                                                  restApiException.getErrorCode()).isEqualTo(
                                                                                  MemberErrorCode.NO_MEMBER);
                                                                            });

  }

  //  @Test
  //  void decreaseCommentLick_ThrowsRestApiException() {
  //    // Given
  //    Long commentId = 1L;
  //    Comment comment1 = new Comment("댓글1", "010-1234-5678"); // 댓글 Mock
  //    when(commentRepository.findById(commentId)).thenReturn(Optional.empty()); // 댓글 없음 설정
  //
  //    // When & Then
  //    assertThatThrownBy(() -> commentService.decreaseCommentLikeCountWithLock(commentId)).isInstanceOf(
  //            RestApiException.class).satisfies(exception -> {
  //      RestApiException restApiException = (RestApiException) exception;
  //      assertThat(restApiException.getErrorCode()).isEqualTo(CommentErrorCode.NO_COMMENT);
  //    }); // 예외 메시지 확인
  //  }


}
