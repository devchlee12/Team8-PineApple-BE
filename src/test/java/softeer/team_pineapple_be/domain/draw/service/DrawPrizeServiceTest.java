package softeer.team_pineapple_be.domain.draw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import softeer.team_pineapple_be.domain.draw.domain.DrawPrize;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.draw.repository.DrawPrizeRepository;
import softeer.team_pineapple_be.domain.draw.repository.DrawRewardInfoRepository;
import softeer.team_pineapple_be.domain.draw.response.DrawRewardInfoResponse;
import softeer.team_pineapple_be.domain.draw.response.SendPrizeResponse;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.cloud.service.MultipartFileWrapper;
import softeer.team_pineapple_be.global.cloud.service.S3DeleteService;
import softeer.team_pineapple_be.global.cloud.service.S3UploadService;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.message.MessageService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DrawPrizeServiceTest {

    @InjectMocks
    private DrawPrizeService drawPrizeService;

    @Mock
    private DrawPrizeRepository drawPrizeRepository;

    @Mock
    private DrawRewardInfoRepository drawRewardInfoRepository;

    @Mock
    private DrawProbabilityService drawProbabilityService;

    @Mock
    private S3UploadService s3UploadService;
    @Mock
    private S3DeleteService s3DeleteService;
    @Mock
    private MessageService messageService;

    @Mock
    private AuthMemberService authMemberService;

    private static final Long PRIZE_ID = 1L;
    private static final String OWNER_PHONE_NUMBER = "010-1234-5678";
    private static final String PRIZE_IMAGE = "prize_image_url";
    private static final Boolean VALID_STATUS = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("상품 당첨자가 맞는 경우 상품을 문자로 성공적으로 전송하는지 - SuccessCase")
    void sendPrizeMessage_WhenOwnerIsValid_SendMessage() {
        // Given
        when(authMemberService.getMemberPhoneNumber()).thenReturn(OWNER_PHONE_NUMBER);
        DrawRewardInfo drawRewardInfo = new DrawRewardInfo(PRIZE_ID.byteValue(), "Prize", 1 ,"image",null);
        DrawPrize prize = new DrawPrize(PRIZE_ID, PRIZE_IMAGE, VALID_STATUS, OWNER_PHONE_NUMBER, drawRewardInfo);
        when(drawPrizeRepository.findById(PRIZE_ID)).thenReturn(Optional.of(prize));

        // When
        drawPrizeService.sendPrizeMessage(PRIZE_ID);
        SendPrizeResponse response = drawPrizeService.sendPrizeMessage(PRIZE_ID);

        // Then
        assertEquals(prize.getDrawRewardInfo().getImage(), response.getImage());
    }

    @Test
    @DisplayName("해당하는 상품이 존재하지 않는 경우 테스트 - FailureCase")
    void sendPrizeMessage_PrizeNotFound_ThrowRestApiException() {
        // Given
        when(authMemberService.getMemberPhoneNumber()).thenReturn(OWNER_PHONE_NUMBER);
        when(drawPrizeRepository.findById(PRIZE_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> drawPrizeService.sendPrizeMessage(PRIZE_ID))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(DrawErrorCode.NO_PRIZE);
                });
    }

    @Test
    @DisplayName("본인이 아닌 다른 경품에 대한 문자 전송을 요청하는 경우 테스트 - FailureCase")
    void sendPrizeMessage_NotPrizeOwner_ThrowRestApiException() {
        // Given
        String differentOwnerPhoneNumber = "987-654-3210";
        when(authMemberService.getMemberPhoneNumber()).thenReturn(differentOwnerPhoneNumber);
        DrawPrize prize = new DrawPrize(PRIZE_ID, PRIZE_IMAGE, VALID_STATUS, OWNER_PHONE_NUMBER, null);
        when(drawPrizeRepository.findById(PRIZE_ID)).thenReturn(Optional.of(prize));

        // When & Then
        assertThatThrownBy(() -> drawPrizeService.sendPrizeMessage(PRIZE_ID))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(DrawErrorCode.NOT_PRIZE_OWNER);
                });
    }

    @Test
    @DisplayName("모든 경품 이미지를 성공적으로 가져오는지 테스트")
    void getDrawRewardImages_ShouldReturnDrawRewardInfoResponses() {
        // Given
        DrawRewardInfo drawRewardInfo1 = new DrawRewardInfo(PRIZE_ID.byteValue(), "Prize", 1 ,"image",null);
        DrawRewardInfo drawRewardInfo2 = new DrawRewardInfo(PRIZE_ID.byteValue(), "Prize2", 2 ,"image2",null);
        when(drawRewardInfoRepository.findAll()).thenReturn(List.of(drawRewardInfo1, drawRewardInfo2));

        // When
        List<DrawRewardInfoResponse> responses = drawPrizeService.getDrawRewardImages();

        // Then
        assertEquals(2, responses.size());
        DrawRewardInfoResponse expectedResponse1 = DrawRewardInfoResponse.of(drawRewardInfo1, drawProbabilityService);
        DrawRewardInfoResponse expectedResponse2 = DrawRewardInfoResponse.of(drawRewardInfo2, drawProbabilityService);

        assertThat(responses.get(0)).usingRecursiveComparison().isEqualTo(expectedResponse1);
        assertThat(responses.get(1)).usingRecursiveComparison().isEqualTo(expectedResponse2);
    }

    @Test
    @DisplayName("ZIP 파일 업로드가 성공적으로 이루어지는지 테스트")
    void uploadDrawPrizeZipFile_ShouldUploadFileSuccessfully() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        String ranking = "1";
        DrawRewardInfo drawRewardInfo = new DrawRewardInfo(PRIZE_ID.byteValue(), "Prize", 1 ,"image",null);
        when(drawRewardInfoRepository.findById(Byte.parseByte(ranking))).thenReturn(Optional.of(drawRewardInfo));
        doNothing().when(s3UploadService).validateZipFile(file);

        doAnswer(invocation -> {
            S3UploadService.FileProcessor processor = invocation.getArgument(2);
            // ZipEntry를 모방하여 파일을 처리
            InputStream mockInputStream = new ByteArrayInputStream(new byte[0]); // Mocking input stream for the zip file
            MultipartFile mockMultipartFile = new MultipartFileWrapper(mockInputStream, "testFile.txt", 1234);
            String fileUrl = "testFileUrl"; // Mock URL

            processor.process(mockMultipartFile, fileUrl); // 파일 처리

            return null;
        }).when(s3UploadService).processZipFile(any(MultipartFile.class), anyString(), any(S3UploadService.FileProcessor.class));

        // When
        drawPrizeService.uploadDrawPrizeZipFile(file, ranking);

        // Then
        verify(s3UploadService).validateZipFile(file);
        verify(s3DeleteService).deleteFolder("draw/1/");
        verify(drawPrizeRepository).deleteByDrawRewardInfoRanking(Byte.parseByte(ranking));
        verify(s3UploadService).processZipFile(eq(file), eq("draw/1/"), any());
    }

    @Test
    @DisplayName("존재하지 않는 경품을 업로드할 경우 예외를 던지는지 테스트")
    void uploadDrawPrizeZipFile_PrizeNotFound_ThrowRestApiException() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        String ranking = "1"; // 존재하지 않는 ranking
        when(drawRewardInfoRepository.findById(Byte.parseByte(ranking))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> drawPrizeService.uploadDrawPrizeZipFile(file, ranking))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(DrawErrorCode.NO_PRIZE);
                });
    }

}