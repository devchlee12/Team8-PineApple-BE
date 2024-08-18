package softeer.team_pineapple_be.domain.draw.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DrawRemainingResponse {
    private List<DrawRemaining> drawRemaining;

    @Getter
    @AllArgsConstructor
    public static class DrawRemaining {
        private Byte ranking;
        private Integer nowStock;
        private Integer totalStock;
    }
}
