package softeer.team_pineapple_be.domain.admin.repisotory;

import org.springframework.data.jpa.repository.JpaRepository;

import softeer.team_pineapple_be.domain.admin.domain.EventDayInfo;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 이벤트 날짜와 몇번째 날인지 매칭하는 엔티티의 리포지토리
 */
public interface EventDayInfoRepository extends JpaRepository<EventDayInfo, Integer> {
    Optional<EventDayInfo> findByEventDate(LocalDate eventDate);
}
