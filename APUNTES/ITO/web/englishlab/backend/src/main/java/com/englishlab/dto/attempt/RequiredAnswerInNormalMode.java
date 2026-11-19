package com.englishlab.dto.attempt;

import com.englishlab.domain.AttemptMode;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validación condicional del answer: obligatorio en modo NORMAL (practica libre) y opcional
 * en modo CHALLENGE (una pregunta puede quedar sin responder por timeout). La violación se
 * ancla al campo {@code answer} para que el @ControllerAdvice la devuelva en `fieldErrors`.
 */
@Documented
@Constraint(validatedBy = RequiredAnswerInNormalMode.Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredAnswerInNormalMode {

    String message() default "La respuesta es obligatoria";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<RequiredAnswerInNormalMode, AttemptRequest> {

        @Override
        public boolean isValid(AttemptRequest value, ConstraintValidatorContext context) {
            if (value == null || AttemptMode.isChallenge(value.mode())) {
                return true;
            }
            if (value.answer() != null && !value.answer().isBlank()) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("answer")
                    .addConstraintViolation();
            return false;
        }
    }
}
