package softeer.team_pineapple_be.domain.draw.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;
import softeer.team_pineapple_be.domain.admin.repisotory.EventDayInfoRepository;
import softeer.team_pineapple_be.domain.comment.exception.CommentErrorCode;
import softeer.team_pineapple_be.domain.comment.repository.CommentRepository;
import softeer.team_pineapple_be.domain.draw.domain.*;
import softeer.team_pineapple_be.domain.draw.exception.DrawErrorCode;
import softeer.team_pineapple_be.domain.draw.repository.*;
import softeer.team_pineapple_be.domain.draw.request.DrawDailyMessageModifyRequest;
import softeer.team_pineapple_be.domain.draw.response.*;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.domain.member.exception.MemberErrorCode;
import softeer.team_pineapple_be.domain.member.repository.MemberRepository;
import softeer.team_pineapple_be.global.auth.service.AuthMemberService;
import softeer.team_pineapple_be.global.cloud.service.S3DeleteService;
import softeer.team_pineapple_be.global.cloud.service.S3UploadService;
import softeer.team_pineapple_be.global.cloud.service.exception.S3ErrorCode;
import softeer.team_pineapple_be.global.exception.RestApiException;

/**
 * 경품 추첨 서비스
 */
@Service
@RequiredArgsConstructor
public class DrawService {
  public static final String DAILY_DRAW_WIN_FOLDER = "daily-win-image/";
  public static final String DAILY_DRAW_LOSE_FOLDER = "daily-lose-image/";
  private final DrawDailyMessageInfoRepository drawDailyMessageInfoRepository;
  private final DrawHistoryRepository drawHistoryRepository;
  private final DrawPrizeRepository drawPrizeRepository;
  private final DrawRewardInfoRepository drawRewardInfoRepository;
  private final AuthMemberService authMemberService;
  private final MemberRepository memberRepository;
  private final RandomDrawPrizeService randomDrawPrizeService;
  private final CommentRepository commentRepository;
  private final S3UploadService s3UploadService;
  private final S3DeleteService s3DeleteService;
  private final DrawProbabilityRepository drawProbabilityRepository;
  private final EventDayInfoRepository eventDayInfoRepository;

  /**
   * 경품 추첨 수행하는 메서드
   *
   * @return 경품에 대한 정보 응답 객체
   */
  @Transactional
  public DrawResponse enterDraw() {
    String memberPhoneNumber = authMemberService.getMemberPhoneNumber();
    Member member = memberRepository.findByPhoneNumber(memberPhoneNumber)
                                    .orElseThrow(() -> new RestApiException(MemberErrorCode.NO_MEMBER));
    canEnterDraw(member);
    member.decrementToolBoxCnt();
    Byte prizeRank = randomDrawPrizeService.drawPrize();
    DrawRewardInfo rewardInfo =
        drawRewardInfoRepository.findById(prizeRank).orElseThrow(() -> new RestApiException(DrawErrorCode.NO_PRIZE));
    DrawDailyMessageInfo dailyMessageInfo = drawDailyMessageInfoRepository.findByDrawDate(LocalDate.now())
                                                                          .orElseThrow(() -> new RestApiException(
                                                                              DrawErrorCode.NOT_VALID_DATE)); // 예외처리
    if (rewardInfo.getRanking() == 0 || rewardInfo.getRanking() == 1 || rewardInfo.getStock() == 0) {
      LocalDate today = LocalDate.now();
      LocalDateTime startOfDay = today.atStartOfDay();
      LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
      drawHistoryRepository.save(new DrawHistory((byte) 0, memberPhoneNumber));
      boolean commentedToday =
          commentRepository.findByPhoneNumberAndPostTimeBetween(memberPhoneNumber, startOfDay, endOfDay).isPresent();
      return new DrawLoseResponse(dailyMessageInfo.getLoseMessage(), dailyMessageInfo.getLoseScenario(),
          dailyMessageInfo.getLoseImage(), member.isCar(), commentedToday, member.getToolBoxCnt());
    }
    drawHistoryRepository.save(new DrawHistory(prizeRank, memberPhoneNumber));
    rewardInfo.decreaseStock();
    Long prizeId = setPrizeOwner(rewardInfo, memberPhoneNumber);
    return new DrawWinningResponse(dailyMessageInfo.getWinMessage(), rewardInfo.getName(),
        dailyMessageInfo.getWinImage(), prizeId, member.isCar(), member.getToolBoxCnt());
  }

  /**
   * 일자별 응모 메시지 가져오기
   *
   * @param date
   * @return
   */
  @Transactional(readOnly = true)
  public DrawDailyMessageResponse getDailyMessageInfo(LocalDate date) {
    DrawDailyMessageInfo dailyMessageInfo = drawDailyMessageInfoRepository.findByDrawDate(date)
                                                                          .orElseThrow(() -> new RestApiException(
                                                                              DrawErrorCode.NO_DAILY_INFO));
    return DrawDailyMessageResponse.builder()
                                   .winMessage(dailyMessageInfo.getWinMessage())
                                   .loseMessage(dailyMessageInfo.getLoseMessage())
                                   .loseScenario(dailyMessageInfo.getLoseScenario())
                                   .winImage(dailyMessageInfo.getWinImage())
                                   .winMessage(dailyMessageInfo.getWinMessage())
                                   .loseImage(dailyMessageInfo.getLoseImage())
                                   .commonScenario(dailyMessageInfo.getCommonScenario())
                                   .drawDate(dailyMessageInfo.getDrawDate())
                                   .build();
  }

  /**
   * 일자별 메시지 정보 수정/등록
   *
   * @param drawDailyMessageModifyRequest
   */
  @Transactional
  public void updateOrSaveDailyMessageInfo(DrawDailyMessageModifyRequest drawDailyMessageModifyRequest) {
    LocalDate drawDate = drawDailyMessageModifyRequest.getDrawDate();
    String winImageFolder = DAILY_DRAW_WIN_FOLDER + drawDate + "/";
    String loseImageFolder = DAILY_DRAW_LOSE_FOLDER + drawDate + "/";

    Optional<DrawDailyMessageInfo> optionalDailyMessageInfo =
            drawDailyMessageInfoRepository.findByDrawDate(drawDate);

    try{
      if (!updateOrSaveDailyMessageInfoCheckTwoExists(drawDailyMessageModifyRequest)) { // 둘 다 없는 경우
        handleUpdateOrSaveForMissingImages(optionalDailyMessageInfo, drawDailyMessageModifyRequest, winImageFolder, loseImageFolder);
      } else if (!updateOrSaveDailyMessageInfoCheckWinImageExists(drawDailyMessageModifyRequest)) { // 루스 이미지만 있는 경우
        handleUpdateOrSaveForMissingWinImage(optionalDailyMessageInfo, drawDailyMessageModifyRequest, loseImageFolder);
      } else if (!updateOrSaveDailyMessageInfoCheckLoseImageExists(drawDailyMessageModifyRequest)) { // 윈 이미지만 있는 경우
        handleUpdateOrSaveForMissingLoseImage(optionalDailyMessageInfo, drawDailyMessageModifyRequest, winImageFolder);
      } else { // 둘 다 있는 경우
        handleUpdateOrSaveForBothImages(optionalDailyMessageInfo, drawDailyMessageModifyRequest, winImageFolder, loseImageFolder);
      }
    }catch (IOException e){
      throw new RestApiException(S3ErrorCode.IMAGE_FAILURE);
    }
  }


  @Transactional
  public DrawRemainingResponse getDrawRemaining(){
    List<DrawRewardInfo> rewardInfos = drawRewardInfoRepository.findAll();
    List<DrawProbability> drawProbabilities = drawProbabilityRepository.findAll();
    List<DrawRemainingResponse.DrawRemaining> drawRemainings = rewardInfos.stream()
            .map(rewardInfo -> {
              Byte ranking = rewardInfo.getRanking();
              Integer nowStock = rewardInfo.getStock();

              Integer totalStock = drawProbabilities.stream()
                      .filter(probability -> probability.getRanking().equals(ranking))
                      .map(DrawProbability::getProbability)
                      .findFirst()
                      .orElse(0);

              return new DrawRemainingResponse.DrawRemaining(ranking, nowStock, totalStock);
            })
            .collect(Collectors.toList());

    return new DrawRemainingResponse(drawRemainings);
  }

  /**
   * 해당 날짜의 응모 시나리오를 조회하는 메서드
   * @return 해당 날짜의 이벤트 진행 일 수와 응모 시나리오
   */
  @Transactional
  public DrawDailyMessageResponse.DrawDailyScenario getDrawDailyScenario(){
    EventDayInfo eventDayInfo = eventDayInfoRepository.findByEventDate(LocalDate.now()).orElseThrow(()-> new RestApiException(DrawErrorCode.NOT_VALID_DATE));
    Integer day = eventDayInfo.getEventDay();
    DrawDailyMessageInfo drawDailyMessageInfo = drawDailyMessageInfoRepository.findByDrawDate(eventDayInfo.getEventDate()).orElseThrow(()-> new RestApiException(DrawErrorCode.NO_DAILY_INFO));
    String commonScenario = drawDailyMessageInfo.getCommonScenario();
    return new DrawDailyMessageResponse.DrawDailyScenario(day, commonScenario);
  }

  /**
   * 경품 추첨 자격 있는지 확인
   *
   * @param member
   */
  private void canEnterDraw(Member member) {
    if ((!member.isCar()) || (member.getToolBoxCnt() == 0)) {
      throw new RestApiException(DrawErrorCode.CANNOT_ENTER_DRAW);
    }
  }

  /**
   * 당첨된 경품에 소유자 설정
   *
   * @param rewardInfo
   * @param memberPhoneNumber
   * @return 경품 ID
   */
  private Long setPrizeOwner(DrawRewardInfo rewardInfo, String memberPhoneNumber) {
    DrawPrize prize = drawPrizeRepository.findFirstByDrawRewardInfoAndValid(rewardInfo, true)
                                         .orElseThrow(() -> new RestApiException(DrawErrorCode.NO_VALID_PRIZE));
    prize.isNowOwnedBy(memberPhoneNumber);
    prize.invalidate();
    return prize.getId();
  }


  private void handleUpdateOrSaveForMissingImages(Optional<DrawDailyMessageInfo> optionalDailyMessageInfo,
                                                  DrawDailyMessageModifyRequest request, String winImageFolder, String loseImageFolder) throws IOException {
    if (optionalDailyMessageInfo.isPresent()) {
      DrawDailyMessageInfo dailyMessageInfo = optionalDailyMessageInfo.get();
      updateDailyMessageInfo(dailyMessageInfo, request, new ImageUrls(dailyMessageInfo.getWinImage(), dailyMessageInfo.getLoseImage()));
    } else {
      throw new RestApiException(DrawErrorCode.NO_DAILY_INFO);
    }
  }

  private void handleUpdateOrSaveForMissingWinImage(Optional<DrawDailyMessageInfo> optionalDailyMessageInfo,
                                                    DrawDailyMessageModifyRequest request, String loseImageFolder) throws IOException {
    if (optionalDailyMessageInfo.isPresent()) {
      DrawDailyMessageInfo dailyMessageInfo = optionalDailyMessageInfo.get();
      s3DeleteService.deleteFolder(loseImageFolder);
      String loseImageUrl = s3UploadService.saveFile(request.getLoseImage(), loseImageFolder);
      updateDailyMessageInfo(dailyMessageInfo, request, new ImageUrls(dailyMessageInfo.getWinImage(), loseImageUrl));
    } else {
      throw new RestApiException(DrawErrorCode.NO_DAILY_INFO);
    }
  }

  private void handleUpdateOrSaveForMissingLoseImage(Optional<DrawDailyMessageInfo> optionalDailyMessageInfo,
                                                     DrawDailyMessageModifyRequest request, String winImageFolder) throws IOException {
    if (optionalDailyMessageInfo.isPresent()) {
      DrawDailyMessageInfo dailyMessageInfo = optionalDailyMessageInfo.get();
      s3DeleteService.deleteFolder(winImageFolder);
      String winImageUrl = s3UploadService.saveFile(request.getWinImage(), winImageFolder);
      updateDailyMessageInfo(dailyMessageInfo, request, new ImageUrls(winImageUrl, dailyMessageInfo.getLoseImage()));
      return;
    }
    throw new RestApiException(DrawErrorCode.NO_DAILY_INFO);

  }

  private void handleUpdateOrSaveForBothImages(Optional<DrawDailyMessageInfo> optionalDailyMessageInfo,
                                               DrawDailyMessageModifyRequest request, String winImageFolder, String loseImageFolder) throws IOException {
    if (optionalDailyMessageInfo.isPresent()) {
      DrawDailyMessageInfo dailyMessageInfo = optionalDailyMessageInfo.get();
      deleteFolders(winImageFolder, loseImageFolder);
      ImageUrls imageUrls = uploadDrawInfoImages(request, winImageFolder, loseImageFolder);
      updateDailyMessageInfo(dailyMessageInfo, request, imageUrls);
      return;
    }
    ImageUrls imageUrls = uploadDrawInfoImages(request, winImageFolder, loseImageFolder);
    drawDailyMessageInfoRepository.save(DrawDailyMessageInfo.builder()
            .winMessage(request.getWinMessage())
            .loseMessage(request.getLoseMessage())
            .loseScenario(request.getLoseScenario())
            .winImage(imageUrls.winImageUrl)
            .loseImage(imageUrls.loseImageUrl)
            .commonScenario(request.getCommonScenario())
            .drawDate(request.getDrawDate())
            .build());
  }

  private void updateDailyMessageInfo(DrawDailyMessageInfo dailyMessageInfo,
                                      DrawDailyMessageModifyRequest request, ImageUrls imageUrls) {
    dailyMessageInfo.update(request.getWinMessage(), request.getLoseMessage(), request.getLoseScenario(),
            imageUrls.winImageUrl, imageUrls.loseImageUrl, request.getCommonScenario(), request.getDrawDate());
  }

  private void deleteFolders(String winImageFolder, String loseImageFolder) {
    s3DeleteService.deleteFolder(winImageFolder);
    s3DeleteService.deleteFolder(loseImageFolder);
  }

  private boolean updateOrSaveDailyMessageInfoCheckTwoExists(DrawDailyMessageModifyRequest request) {
    return request.getWinImage() != null || request.getLoseImage() != null;
  }

  private boolean updateOrSaveDailyMessageInfoCheckWinImageExists(DrawDailyMessageModifyRequest request) {
    return request.getWinImage() != null;
  }

  private boolean updateOrSaveDailyMessageInfoCheckLoseImageExists(DrawDailyMessageModifyRequest request) {
    return request.getLoseImage() != null;
  }

  private ImageUrls uploadDrawInfoImages(DrawDailyMessageModifyRequest request, String winImageFolder, String loseImageFolder) throws IOException {
    String winImageUrl = s3UploadService.saveFile(request.getWinImage(), winImageFolder);
    String loseImageUrl = s3UploadService.saveFile(request.getLoseImage(), loseImageFolder);
    return new ImageUrls(winImageUrl, loseImageUrl);
  }

  private record ImageUrls(String winImageUrl, String loseImageUrl) {
  }
}
