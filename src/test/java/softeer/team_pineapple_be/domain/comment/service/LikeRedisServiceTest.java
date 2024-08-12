package softeer.team_pineapple_be.domain.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import softeer.team_pineapple_be.domain.comment.domain.CommentLike;
import softeer.team_pineapple_be.domain.comment.domain.id.LikeId;
import softeer.team_pineapple_be.domain.comment.repository.CommentLikeRepository;
import softeer.team_pineapple_be.global.auth.context.AuthContext;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LikeRedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private AuthMemberService authMemberService;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    private LikeRedisService likeRedisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        likeRedisService = new LikeRedisService(redisTemplate, authMemberService, commentLikeRepository);
    }

    @Test
    void testAddLike() {
        String memberPhoneNumber = "1234567890";
        Long commentId = 1L;

        when(authMemberService.getMemberPhoneNumber()).thenReturn(memberPhoneNumber);
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        likeRedisService.addLike(commentId);

        verify(setOps).add("comment:" + commentId, memberPhoneNumber);
    }

    @Test
    void testInitializeRedisStorage() {
        List<CommentLike> allLikes = Arrays.asList(
                new CommentLike(new LikeId(1L, "1234567890")),
                new CommentLike(new LikeId(2L, "0987654321"))
        );

        when(commentLikeRepository.findAll()).thenReturn(allLikes);
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        likeRedisService.initializeRedisStorage();

        verify(setOps, times(1)).add("comment:" + 1L, "1234567890");
        verify(setOps, times(1)).add("comment:" + 2L, "0987654321");
    }

    @Test
    void testIsLiked_WhenLiked() {
        Long commentId = 1L;
        String memberPhoneNumber = "1234567890";

        AuthContext authContext = mock(AuthContext.class);
        when(authMemberService.getAuthContext()).thenReturn(authContext);
        when(authContext.getPhoneNumber()).thenReturn(memberPhoneNumber);

        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember("comment:" + commentId, memberPhoneNumber)).thenReturn(true);

        Boolean result = likeRedisService.isLiked(commentId);

        assertThat(result).isTrue();
    }

    @Test
    void testIsLiked_AuthContextNull1() {
        Long commentId = 1L;

        // authMemberService.getAuthContext()가 null을 반환하도록 설정
        when(authMemberService.getAuthContext()).thenReturn(null);

        // 메서드 호출
        Boolean result = likeRedisService.isLiked(commentId);

        // 결과 검증
        assertThat(result).isFalse();
    }

    @Test
    void testIsLiked_WhenNotLiked() {
        Long commentId = 1L;
        String memberPhoneNumber = "1234567890";

        AuthContext authContext = mock(AuthContext.class);
        when(authMemberService.getAuthContext()).thenReturn(authContext);
        when(authContext.getPhoneNumber()).thenReturn(memberPhoneNumber);

        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember("comment:" + commentId, memberPhoneNumber)).thenReturn(false);

        Boolean result = likeRedisService.isLiked(commentId);

        assertThat(result).isFalse();
    }

    @Test
    void testRemoveLike() {
        String memberPhoneNumber = "1234567890";
        Long commentId = 1L;

        when(authMemberService.getMemberPhoneNumber()).thenReturn(memberPhoneNumber);
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        likeRedisService.removeLike(commentId);

        verify(setOps).remove("comment:" + commentId, memberPhoneNumber);
    }
}

