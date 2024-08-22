package softeer.team_pineapple_be.domain.draw.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.draw.domain.DrawPrize;
import softeer.team_pineapple_be.domain.draw.domain.DrawRewardInfo;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.draw.repository.DrawPrizeRepository;
import softeer.team_pineapple_be.domain.draw.repository.DrawRewardInfoRepository;
import softeer.team_pineapple_be.domain.draw.response.DrawRewardInfoResponse;
import softeer.team_pineapple_be.domain.draw.response.SendPrizeResponse;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.cloud.service.S3DeleteService;
import softeer.team_pineapple_be.global.cloud.service.S3UploadService;
import softeer.team_pineapple_be.global.exception.RestApiException;
import softeer.team_pineapple_be.global.message.MessageService;

/**
 * 경품 서비스
 */
@Service
@RequiredArgsConstructor
public class DrawPrizeService {
  private final DrawPrizeRepository drawPrizeRepository;
  private final MessageService messageService;
  private final AuthMemberService authMemberService;
  private final S3UploadService s3UploadService;
  private final S3DeleteService s3DeleteService;
  private final DrawRewardInfoRepository drawRewardInfoRepository;
  private final DrawProbabilityService drawProbabilityService;

  /**
   * 응모 경품 이미지를 반환하는 메서드
   */
  @Transactional(readOnly = true)
  public List<DrawRewardInfoResponse> getDrawRewardImages() {
    List<DrawRewardInfo> all = drawRewardInfoRepository.findAll();
    return all.stream().map(info -> DrawRewardInfoResponse.of(info, drawProbabilityService)).toList();
  }

  /**
   * 경품을 전송하는 메서드
   *
   * @param prizeId 전송하고자 하는 상품의 id
   * @return 상품 전송 결과
   */
  @Transactional
  public SendPrizeResponse sendPrizeMessage(Long prizeId) {
    String memberPhoneNumber = authMemberService.getMemberPhoneNumber();
    DrawPrize prize =
        drawPrizeRepository.findById(prizeId).orElseThrow(() -> new RestApiException(DrawErrorCode.NO_PRIZE));
    if (!memberPhoneNumber.equals(prize.getOwner())) {
      throw new RestApiException(DrawErrorCode.NOT_PRIZE_OWNER);
    }
    messageService.sendPrizeImage(prize.getImage());
    String rewardInfoImage = prize.getDrawRewardInfo().getImage();
    return new SendPrizeResponse(rewardInfoImage);
  }

  /**
   * ZIP 파일을 업로드하여 드로우 상품 이미지를 S3에 저장하고 DB에 정보 등록
   *
   * @param file    업로드할 ZIP 파일
   * @param ranking 드로우 랭킹 정보
   * @throws RestApiException 파일 형식이 ZIP이 아닌 경우 또는 권한이 없는 경우 발생
   */
  @Transactional
  public void uploadDrawPrizeZipFile(MultipartFile file, String ranking) {
    s3UploadService.validateZipFile(file);

    DrawRewardInfo drawRewardInfo = drawRewardInfoRepository.findById(Byte.parseByte(ranking))
                                                            .orElseThrow(
                                                                () -> new RestApiException(DrawErrorCode.NO_PRIZE));

    String fileName = "draw/" + ranking + "/";
    s3DeleteService.deleteFolder(fileName);
    drawPrizeRepository.deleteByDrawRewardInfoRanking(Byte.parseByte(ranking));

    List<DrawPrize> drawPrizes = new ArrayList<>();
    s3UploadService.processZipFile(file, fileName, (multipartFile, fileUrl) -> {
      DrawPrize drawPrize = new DrawPrize(fileUrl, true, null, drawRewardInfo);
      drawPrizes.add(drawPrize);
    });

    drawPrizeRepository.saveAll(drawPrizes);
  }
}
