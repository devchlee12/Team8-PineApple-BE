package softeer.team_pineapple_be.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 멤버 엔티티
 */
@Entity
@NoArgsConstructor
@Getter
public class Member {
  @Id
  @Column(nullable = false)
  private String phoneNumber;

  @Column(nullable = false)
  private Integer toolBoxCnt;

  @Column(nullable = false)
  private boolean car;

  @Column(nullable = false)
  private String role;

  public Member(String phoneNumber) {
    this.phoneNumber = phoneNumber;
    toolBoxCnt = 0;
    car = false;
    role = "USER";
  }

  public void decrementToolBoxCnt() {
    this.toolBoxCnt -= 1;
  }

  /**
   * 월드컵 참여 기록
   */
  public void generateCar() {
    this.car = true;
  }

  public void incrementToolBoxCnt() {
    this.toolBoxCnt += 1;
  }

  public void increment10ToolBoxCnt() {
    this.toolBoxCnt += 10;
  }
}
