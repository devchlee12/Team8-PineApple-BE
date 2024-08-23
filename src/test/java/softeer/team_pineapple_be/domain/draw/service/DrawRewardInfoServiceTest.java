package softeer.team_pineapple_be.domain.draw.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;
import softeer.team_pineapple_be.domain.draw.repository.DrawRewardInfoRepository;
import softeer.team_pineapple_be.domain.draw.request.DrawRewardInfoListRequest;
import softeer.team_pineapple_be.domain.draw.response.DrawRewardInfoListResponse;
import softeer.team_pineapple_be.domain.member.exception.MemberErrorCode;
import softeer.team_pineapple_be.global.cloud.service.S3DeleteService;
import softeer.team_pineapple_be.global.cloud.service.S3UploadService;
import softeer.team_pineapple_be.global.cloud.service.exception.S3ErrorCode;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DrawRewardInfoServiceTest {

    @InjectMocks
    private DrawRewardInfoService drawRewardInfoService;

    @Mock
    private DrawRewardInfoRepository drawRewardInfoRepository;

    @Mock
    private S3UploadService s3UploadService;

    @Mock
    private S3DeleteService s3DeleteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllDrawRewardInfo() {
        // given
        DrawRewardInfo rewardInfo1 = new DrawRewardInfo((byte) 1, "Prize 1", 10, "image1.jpg");
        DrawRewardInfo rewardInfo2 = new DrawRewardInfo((byte) 2, "Prize 2", 5, "image2.jpg");
        List<DrawRewardInfo> rewardInfos = Arrays.asList(rewardInfo1, rewardInfo2);

        when(drawRewardInfoRepository.findAll()).thenReturn(rewardInfos);

        // when
        DrawRewardInfoListResponse response = drawRewardInfoService.getAllDrawRewardInfo();

        // then
        assertThat(response.getRewards()).hasSize(2);
        assertThat(response.getRewards().get(0).getName()).isEqualTo("Prize 1");
        assertThat(response.getRewards().get(1).getName()).isEqualTo("Prize 2");
    }

    @Test
    void testSetDrawRewardInfoList_WithImageUpload() throws IOException {
        // given
        DrawRewardInfoListRequest request = new DrawRewardInfoListRequest();
        DrawRewardInfoListRequest.DrawRewardInfoRequest rewardInfoRequest = new DrawRewardInfoListRequest.DrawRewardInfoRequest((byte) 1, "Prize 1", 10, mock(MultipartFile.class));
        request.setRewards(Arrays.asList(rewardInfoRequest));

        String fileName = "drawRewardInfo/1/";
        when(s3UploadService.saveFile(any(), eq(fileName))).thenReturn("uploadedImage.jpg");

        // when
        drawRewardInfoService.setDrawRewardInfoList(request);

        // then
        verify(s3DeleteService).deleteFolder(fileName);
        verify(s3UploadService).saveFile(any(), eq(fileName));
        verify(drawRewardInfoRepository).saveAll(anyList());
    }

    @Test
    void testSetDrawRewardInfoList_WithoutImageUpload() {
        // given
        DrawRewardInfo existingRewardInfo = new DrawRewardInfo((byte) 1, "Prize 1", 10, "existingImage.jpg");
        DrawRewardInfoListRequest request = new DrawRewardInfoListRequest();
        DrawRewardInfoListRequest.DrawRewardInfoRequest rewardInfoRequest = new DrawRewardInfoListRequest.DrawRewardInfoRequest((byte) 1, "Prize 1", 10, null);
        request.setRewards(Arrays.asList(rewardInfoRequest));

        when(drawRewardInfoRepository.findById((byte) 1)).thenReturn(Optional.of(existingRewardInfo));

        // when
        drawRewardInfoService.setDrawRewardInfoList(request);

        // then
        verify(s3DeleteService, never()).deleteFolder(any()); // 이미지가 없으므로 삭제하지 않음
        verify(drawRewardInfoRepository).saveAll(anyList());
    }

    @Test
    void testSetDrawRewardInfoList_ImageUploadIOException() throws IOException {
        // given
        DrawRewardInfoListRequest request = new DrawRewardInfoListRequest();
        DrawRewardInfoListRequest.DrawRewardInfoRequest rewardInfoRequest = new DrawRewardInfoListRequest.DrawRewardInfoRequest((byte) 1, "Prize 1", 10, mock(MultipartFile.class));
        request.setRewards(Arrays.asList(rewardInfoRequest));

        when(s3UploadService.saveFile(any(), any())).thenThrow(new IOException("Upload error"));

        // when / then
        assertThatThrownBy(() -> drawRewardInfoService.setDrawRewardInfoList(request))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception -> {
                    RestApiException restApiException = (RestApiException) exception; // 캐스팅
                    assertThat(restApiException.getErrorCode()).isEqualTo(S3ErrorCode.IMAGE_FAILURE);
                });
    }
}
