package softeer.team_pineapple_be.domain.comment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        comments = new ArrayList<>();
        when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
        for (int i = 1; i <= 5; i++) {
            Comment comment = new Comment((long) i, phoneNumber,i + phoneNumber, i, LocalDateTime.now().plusMinutes(i));
            comments.add(comment);
        }
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
        commentService.saveComment(commentRequest);

        // Then
        verify(memberRepository).findById(phoneNumber);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("saveComment: 성공적으로 기대평을 저장한다.")
    void saveComment_MemberNotExists() {
        // Given
        CommentRequest commentRequest = new CommentRequest("좋은 제품입니다.");
        when(memberRepository.findById(phoneNumber)).thenReturn(Optional.empty());
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());

        //// When & Then
        //            assertThatThrownBy(() -> quizService.quizHistory())
        //                    .isInstanceOf(RestApiException.class)
        //                    .satisfies(exception -> {
        //                        RestApiException restApiException = (RestApiException) exception; // 캐스팅
        //                        assertThat(restApiException.getErrorCode()).isEqualTo(QuizErrorCode.PARTICIPATION_EXISTS);
        //                    });
        // When & Then
        assertThatThrownBy(() -> commentService.saveComment(commentRequest))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception;
                    assertThat(restApiException.getErrorCode()).isEqualTo(MemberErrorCode.NO_MEMBER);
                });
    }

    @Test
    @DisplayName("saveComment: 오늘 이미 기대평을 작성한 경우 예외가 발생한다.")
    void saveComment_AlreadyReviewed() {
        // Given
        CommentRequest commentRequest = new CommentRequest("좋은 제품입니다.");
        when(memberRepository.findById(phoneNumber)).thenReturn(Optional.of(new Member(phoneNumber)));
        when(commentRepository.findByPhoneNumberAndPostTimeBetween(eq(phoneNumber), any(), any())).thenReturn(Optional.of(new Comment()));

        // When & Then
        assertThatThrownBy(() -> commentService.saveComment(commentRequest))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception;
                    assertThat(restApiException.getErrorCode()).isEqualTo(CommentErrorCode.ALREADY_REVIEWED);
                });
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
        commentService.saveCommentLike(commentLikeRequest);

        // Then
        verify(commentLikeRepository).save(any(CommentLike.class));
        verify(likeRedisService).addLike(comment.getId());
        assertThat(commentLikeRequest.getCommentId()).isEqualTo(1L);
    }


    @Test
    @DisplayName("saveCommentLike: 좋아요를 성공적으로 추가한다.")
    void saveCommentLike_CommentNotExists() {
        // Given
        CommentLikeRequest commentLikeRequest = new CommentLikeRequest(1L);
        Comment comment = new Comment("테스트 내용", phoneNumber);
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        when(commentLikeRepository.findById(any(LikeId.class))).thenReturn(Optional.empty());

        // When

        // When & Then
        assertThatThrownBy(() -> commentService.saveCommentLike(commentLikeRequest))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception;
                    assertThat(restApiException.getErrorCode()).isEqualTo(CommentErrorCode.NO_COMMENT);
                });
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
        commentService.saveCommentLike(commentLikeRequest);

        // Then
        verify(commentLikeRepository).delete(any(CommentLike.class));
        verify(likeRedisService).removeLike(comment.getId());
        assertThat(comment.getLikeCount()).isEqualTo(0);
    }

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
                .map(comment -> CommentResponse.fromComment(comment, likeRedisService)) // LikeRedisService는 필요에 따라 조정
                .toList();

        // CommentPageResponse 생성
        CommentPageResponse mockResponse = new CommentPageResponse(mockPage.getTotalPages(), commentResponseList);

        // Mocking commentDao의 메서드
        when(commentDao.getCommentsSortedByRecent(anyInt(), any(LocalDate.class), any(LikeRedisService.class)))
                .thenReturn(mockResponse);

        // When
        CommentPageResponse response = commentService.getCommentsSortedByRecent(0, date);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getComments()).hasSize(5);
        assertThat(response.getComments().get(0).getId()).isEqualTo(1); // 가장 최근 댓글 확인
    }
}
