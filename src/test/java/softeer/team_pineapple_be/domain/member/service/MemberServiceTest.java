package softeer.team_pineapple_be.domain.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.domain.member.exception.MemberErrorCode;
import softeer.team_pineapple_be.domain.member.repository.MemberRepository;
import softeer.team_pineapple_be.domain.member.response.MemberInfoResponse;
import softeer.team_pineapple_be.domain.quiz.service.QuizRedisService;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.cloud.service.exception.S3ErrorCode;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthMemberService authMemberService;

    @Mock
    private QuizRedisService quizRedisService;

    private Member member;
    private String phoneNumber;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        phoneNumber = "010-1234-5678";
        member = new Member(phoneNumber);

    }

    @Test
    @DisplayName("회원 정보를 정상적으로 조회할 수 있다.")
    void getMemberInfo() {
        // Given
        when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
        when(memberRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(member));
        when(quizRedisService.wasParticipatedInQuiz(phoneNumber)).thenReturn(true);

        // When
        MemberInfoResponse response = memberService.getMemberInfo();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPhoneNumber()).isEqualTo(phoneNumber);
        verify(memberRepository).findByPhoneNumber(phoneNumber);
        verify(authMemberService).getMemberPhoneNumber();
        verify(quizRedisService).wasParticipatedInQuiz(phoneNumber);
    }

    @Test
    @DisplayName("회원 정보가 없을 경우 예외가 발생한다.")
    void getMemberInfo_NoMember() {
        // Given
        when(authMemberService.getMemberPhoneNumber()).thenReturn(phoneNumber);
        when(memberRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.getMemberInfo())
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(MemberErrorCode.NO_MEMBER);
                });
    }
}

