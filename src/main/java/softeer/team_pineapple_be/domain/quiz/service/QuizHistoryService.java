package softeer.team_pineapple_be.domain.quiz.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;
import softeer.team_pineapple_be.domain.admin.repisotory.EventDayInfoRepository;
import softeer.team_pineapple_be.domain.member.domain.Member;
import softeer.team_pineapple_be.domain.quiz.domain.QuizHistory;
import softeer.team_pineapple_be.domain.quiz.repository.QuizHistoryRepository;
import softeer.team_pineapple_be.domain.quiz.response.QuizHistoryResponse;

import java.time.LocalDate;
import java.util.*;

/**
 * 퀴즈 기록을 통하여 지표를 분석해주는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class QuizHistoryService {

    private final QuizHistoryRepository quizHistoryRepository;
    private final EventDayInfoRepository eventDayInfoRepository;
    private final int SCHEDULE_LENGTH = 14;
    private int totalDays;


    /**
     * Day N Retention 과 DAU를 계산해주는 메서드
     * @return 지표의 정보가 담긴 2차원 배열
     */
    @Transactional
    public QuizHistoryResponse getDayNRetentionAndDAU() {
        determineTotalDays();
        double[][] retentionRates = initializeRetentionRates();
        int[] dau = new int[totalDays];
        List<QuizHistory> allHistories = quizHistoryRepository.findAll();


        Set<Member> allMembers = new HashSet<>();
        Map<Integer, Set<Member>> allMemberMap = createAllMemberMap(allHistories, allMembers);
        Map<Integer, Set<Member>> dayMemberMap = createDayMemberMap(allHistories);
        calculateRetentionRates(retentionRates, dau, allMemberMap, dayMemberMap);

        return new QuizHistoryResponse(retentionRates, dau);
    }

    private void determineTotalDays(){
        Optional<EventDayInfo> eventDayInfo = eventDayInfoRepository.findByEventDate(LocalDate.now());
        if(eventDayInfo.isEmpty()){
            totalDays = SCHEDULE_LENGTH;
            return;
        }
        totalDays = eventDayInfo.get().getEventDay();
    }

    private double[][] initializeRetentionRates() {
        double[][] retentionRates = new double[totalDays][totalDays];
        initializeArray(retentionRates, -1.0);
        return retentionRates;
    }

    private void initializeArray(double[][] array, double value) {
        for (double[] row : array) {
            Arrays.fill(row, value);
        }
    }

    private void calculateRetentionRates(double[][] retentionRates, int[] dau, Map<Integer, Set<Member>> allMemberMap, Map<Integer, Set<Member>> dayMemberMap) {
        for (int startDay = 1; startDay <= totalDays; startDay++) {
            Set<Member> startDayMembers = getMembersForDay(startDay, allMemberMap);
            Set<Member> dayMembers = getMembersForDay(startDay, dayMemberMap);

            if (dayMembers == null || dayMembers.isEmpty()) {
                handleEmptyDayMembers(retentionRates, startDay);
                continue;
            }

            dau[startDay - 1] = dayMembers.size();

            if (startDayMembers == null || startDayMembers.isEmpty()) {
                retentionRates[startDay - 1][0] = 0;
                continue;
            }

            retentionRates[startDay - 1][0] = startDayMembers.size();
            calculateRetentionForSubsequentDays(retentionRates, startDayMembers, startDay, dayMemberMap);
        }
    }

    private void handleEmptyDayMembers(double[][] retentionRates, int startDay) {
        retentionRates[startDay - 1][0] = 0;
        for (int targetDay = startDay + 1; targetDay <= totalDays; targetDay++) {
            retentionRates[startDay - 1][targetDay - 1] = 0;
        }
    }

    private void calculateRetentionForSubsequentDays(double[][] retentionRates, Set<Member> startDayMembers, int startDay, Map<Integer, Set<Member>> dayMemberMap) {
        for (int targetDay = startDay + 1; targetDay <= totalDays; targetDay++) {
            Set<Member> targetDayMembers = getMembersForDay(targetDay, dayMemberMap);
            retentionRates[startDay - 1][targetDay - 1] = calculateRetentionOrZero(startDayMembers, targetDayMembers);
        }
    }

    private double calculateRetentionOrZero(Set<Member> startDayMembers, Set<Member> targetDayMembers) {
        if (targetDayMembers == null || targetDayMembers.isEmpty()) {
            return 0.0;
        }
        return calculateRetentionRate(startDayMembers, targetDayMembers);
    }


    private Set<Member> getMembersForDay(int day, Map<Integer, Set<Member>> memberMap) {
        return memberMap.get(day);
    }

    private double calculateRetentionRate(Set<Member> startDayMembers, Set<Member> targetDayMembers) {
        long retainedCount = targetDayMembers.stream().filter(startDayMembers::contains).count();
        double retentionRate = (double) retainedCount / startDayMembers.size() * 100;
        return Math.round(retentionRate * 100.0) / 100.0;
    }

    private Map<Integer, Set<Member>> createAllMemberMap(List<QuizHistory> allHistories, Set<Member> allMembers) {
        Map<Integer, Set<Member>> memberMap = new HashMap<>();

        for (QuizHistory history : allHistories) {
            Member member = history.getMember();
            if (allMembers.contains(member)) {
                continue;
            }
            Integer quizId = history.getQuizContent().getId();
            memberMap.putIfAbsent(quizId, new HashSet<>());
            memberMap.get(quizId).add(member);
            allMembers.add(member);
        }

        return memberMap;
    }

    private Map<Integer, Set<Member>> createDayMemberMap(List<QuizHistory> allHistories) {
        Map<Integer, Set<Member>> memberMap = new HashMap<>();
        for (QuizHistory history : allHistories) {
            Member member = history.getMember();
            Integer quizId = history.getQuizContent().getId();
            memberMap.putIfAbsent(quizId, new HashSet<>());
            memberMap.get(quizId).add(member);
        }

        return memberMap;
    }
}
