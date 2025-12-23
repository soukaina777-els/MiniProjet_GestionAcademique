package exrc_miniprojet.util;

public final class Validator {
    private Validator() {}

    public static void notBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new DaoException("Champ obligatoire : " + fieldName);
        }
    }

    public static void email(String value) {
        notBlank(value, "Email");
        if (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new DaoException("Email invalide.");
        }
    }
}
