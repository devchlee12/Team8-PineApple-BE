package softeer.team_pineapple_be.domain.draw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.draw.repository.DrawRewardInfoRepository;
import softeer.team_pineapple_be.domain.draw.request.DrawRewardInfoListRequest;
import softeer.team_pineapple_be.domain.draw.response.DrawRewardInfoListResponse;
import softeer.team_pineapple_be.global.cloud.service.S3DeleteService;
import softeer.team_pineapple_be.global.cloud.service.S3UploadService;
import softeer.team_pineapple_be.global.exception.RestApiException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 경품 상품 서비스
 */
@Service
@RequiredArgsConstructor
public class DrawRewardInfoService {
    private static final String DRAW_REWAR_INFO_FOLDER = "drawRewardInfo/";
    private final DrawRewardInfoRepository drawRewardInfoRepository;
    private final S3UploadService s3UploadService;
    private final S3DeleteService s3DeleteService;


    /**
     * 모든 경품에 대한 조회를 하는 메서드
     * @return 경품 리스트
     */
    @Transactional(readOnly = true)
    public DrawRewardInfoListResponse getAllDrawRewardInfo() {
        List<DrawRewardInfo> rewardInfos = drawRewardInfoRepository.findAll();
        List<DrawRewardInfoListResponse.DrawRewardInfoResponse> rewardInfoResponses = rewardInfos.stream()
                .map(rewardInfo -> new DrawRewardInfoListResponse.DrawRewardInfoResponse(rewardInfo.getRanking(), rewardInfo.getName(), rewardInfo.getStock(), rewardInfo.getImage()))
                .collect(Collectors.toList());

        return new DrawRewardInfoListResponse(rewardInfoResponses);
    }

    /**
     * 모든 경품에 대한 수정을 하는 메서드
     * @param request drawRewardInfo에 대한 리스트
     */
    @Transactional
    public void setDrawRewardInfoList(DrawRewardInfoListRequest request) {
        List<DrawRewardInfo> rewardInfoList = request.getRewards().stream()
                .map(rewardInfoRequest -> {
                    if(rewardInfoRequest.getImage() == null){
                        DrawRewardInfo drawRewardInfo = drawRewardInfoRepository.findById(rewardInfoRequest.getRanking()).orElseThrow(() -> new RestApiException(DrawErrorCode.NO_PRIZE));
                        return new DrawRewardInfo(rewardInfoRequest.getRanking(),
                                rewardInfoRequest.getName(),
                                rewardInfoRequest.getStock(),
                                drawRewardInfo.getImage());
                    }
                    String fileName = DRAW_REWAR_INFO_FOLDER + rewardInfoRequest.getRanking() + "/";
                    s3DeleteService.deleteFolder(fileName);
                    String image;
                    try {
                        image = s3UploadService.saveFile(rewardInfoRequest.getImage(), fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return new DrawRewardInfo(
                            rewardInfoRequest.getRanking(),
                            rewardInfoRequest.getName(),
                            rewardInfoRequest.getStock(),
                            image
                    );
                })
                .collect(Collectors.toList());

        drawRewardInfoRepository.saveAll(rewardInfoList);
    }
}
