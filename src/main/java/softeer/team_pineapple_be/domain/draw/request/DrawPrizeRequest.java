package softeer.team_pineapple_be.domain.draw.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DrawPrizeRequest {
    private MultipartFile file;
    @Range(min = 1, max = 4, message = "{draw.prize_ranking_range}")
    private String ranking;
}
