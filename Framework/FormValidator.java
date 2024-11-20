package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import annotation.*;
import exception.*;

public class FormValidator {

    public static void validate(Object formObject) throws ValidationException {
        Field[] fields = formObject.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(formObject);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access field value", e);
            }

            for (Annotation annotation : field.getAnnotations()) {
                if (annotation instanceof Required) {
                    validateRequired(field, value);
                } else if (annotation instanceof Numeric) {
                    validateNumeric(field, value);
                } else if (annotation instanceof Min) {
                    validateMin(field, value, ((Min) annotation).value());
                } else if (annotation instanceof Max) {
                    validateMax(field, value, ((Max) annotation).value());
                } else if (annotation instanceof Range) {
                    validateRange(field, value, ((Range) annotation).min(), ((Range) annotation).max());
                } else if (annotation instanceof LengthMin) {
                    validateLengthMin(field, value, ((LengthMin) annotation).value());
                } else if (annotation instanceof LengthMax) {
                    validateLengthMax(field, value, ((LengthMax) annotation).value());
                } else if (annotation instanceof RegexPattern) {
                    validatePattern(field, value, ((RegexPattern) annotation).regex());
                } else if (annotation instanceof Email) {
                    validateEmail(field, value);
                } else if (annotation instanceof DateFormat) {
                    validateDateFormat(field, value, ((DateFormat) annotation).pattern());
                }
            }
        }
    }
    public static void validateField(Field field, Object value) throws ValidationException {
        field.setAccessible(true);

        for (Annotation annotation : field.getAnnotations()) {
            if (annotation instanceof Required) {
                validateRequired(field, value);
            } else if (annotation instanceof Numeric) {
                validateNumeric(field, value);
            } else if (annotation instanceof Min) {
                validateMin(field, value, ((Min) annotation).value());
            } else if (annotation instanceof Max) {
                validateMax(field, value, ((Max) annotation).value());
            } else if (annotation instanceof Range) {
                validateRange(field, value, ((Range) annotation).min(), ((Range) annotation).max());
            } else if (annotation instanceof LengthMin) {
                validateLengthMin(field, value, ((LengthMin) annotation).value());
            } else if (annotation instanceof LengthMax) {
                validateLengthMax(field, value, ((LengthMax) annotation).value());
            } else if (annotation instanceof RegexPattern) {
                validatePattern(field, value, ((RegexPattern) annotation).regex());
            } else if (annotation instanceof Email) {
                validateEmail(field, value);
            } else if (annotation instanceof DateFormat) {
                validateDateFormat(field, value, ((DateFormat) annotation).pattern());
            }
        }
    }


    private static void validateRequired(Field field, Object value) throws ValidationException {
        Required annotation = field.getAnnotation(Required.class);
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            throw new ValidationException((field.getName() + " is required"));
        }
    }

    private static void validateNumeric(Field field, Object value) throws ValidationException {
        Numeric annotation = field.getAnnotation(Numeric.class);
        if (value != null && !(value instanceof Number)) {
            try {
                Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                throw new ValidationException((field.getName() + " must be a numeric value"));
            }
        }
    }

    private static void validateMin(Field field, Object value, int minValue) throws ValidationException {
        Min annotation = field.getAnnotation(Min.class);
        if (value instanceof Number && ((Number) value).doubleValue() < minValue) {
            throw new ValidationException((field.getName() + " must be greater than or equal to " + minValue));
        }
    }

    private static void validateMax(Field field, Object value, int maxValue) throws ValidationException {
        Max annotation = field.getAnnotation(Max.class);
        if (value instanceof Number && ((Number) value).doubleValue() > maxValue) {
            throw new ValidationException((field.getName() + " must be less than or equal to " + maxValue));
        }
    }

    private static void validateRange(Field field, Object value, int min, int max) throws ValidationException {
        Range annotation = field.getAnnotation(Range.class);
        if (value instanceof Number) {
            double numValue = ((Number) value).doubleValue();
            if (numValue < min || numValue > max) {
                throw new ValidationException((field.getName() + " must be between " + min + " and " + max));
            }
        }
    }

    private static void validateLengthMin(Field field, Object value, int minLength) throws ValidationException {
        LengthMin annotation = field.getAnnotation(LengthMin.class);
        if (value instanceof String && ((String) value).length() < minLength) {
            throw new ValidationException((field.getName() + " must have at least " + minLength + " characters"));
        }
    }

    private static void validateLengthMax(Field field, Object value, int maxLength) throws ValidationException {
        LengthMax annotation = field.getAnnotation(LengthMax.class);
        if (value instanceof String && ((String) value).length() > maxLength) {
            throw new ValidationException((field.getName() + " must have at most " + maxLength + " characters"));
        }
    }

    private static void validatePattern(Field field, Object value, String regex) throws ValidationException {
        RegexPattern annotation = field.getAnnotation(RegexPattern.class);
        if (value instanceof String && !Pattern.matches(regex, (String) value)) {
            throw new ValidationException((field.getName() + " must match the pattern: " + regex));
        }
    }

    private static void validateEmail(Field field, Object value) throws ValidationException {
        Email annotation = field.getAnnotation(Email.class);
        String emailRegex = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (value instanceof String && !Pattern.matches(emailRegex, (String) value)) {
            throw new ValidationException((field.getName() + " must be a valid email address"));
        }
    }

    private static void validateDateFormat(Field field, Object value, String pattern) throws ValidationException {
        DateFormat annotation = field.getAnnotation(DateFormat.class);
        if (value instanceof String) {
            try {
                new java.text.SimpleDateFormat(pattern).parse((String) value);
            } catch (java.text.ParseException e) {
                throw new ValidationException((field.getName() + " must match the date format: " + pattern));
            }
        }
    }
}
