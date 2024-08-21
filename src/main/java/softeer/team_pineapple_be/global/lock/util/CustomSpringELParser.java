package softeer.team_pineapple_be.global.lock.util;

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.experimental.UtilityClass;

/**
 * Lock 이름 Spring EL로 파싱해서 읽어옴
 */
@UtilityClass
public class CustomSpringELParser {
  public Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
    SpelExpressionParser parser = new SpelExpressionParser();
    StandardEvaluationContext context = new StandardEvaluationContext();

    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }

    return parser.parseExpression(key).getValue(context, Object.class);
  }
}
