package softeer.team_pineapple_be.domain.quiz.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizRewardUploadRequest{
    private MultipartFile file;
    private LocalDate quizDate;
}
