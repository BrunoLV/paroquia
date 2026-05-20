package br.com.nsfatima.gestao.calendario.api.v1.dto.support;

public final class EnumRequestNormalizer {

    private EnumRequestNormalizer() {
    }

    public static <E extends Enum<E>> E normalize(String rawValue, Class<E> enumType) {
        if (rawValue == null) {
            return null;
        }

        String candidate = rawValue.trim();
        if (candidate.isEmpty()) {
            throw new IllegalArgumentException("Enum value must not be blank");
        }

        for (E constant : enumType.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(candidate)) {
                return constant;
            }
        }

        throw new IllegalArgumentException(
                "Unsupported enum value '%s' for %s".formatted(rawValue, enumType.getSimpleName()));
    }
}
