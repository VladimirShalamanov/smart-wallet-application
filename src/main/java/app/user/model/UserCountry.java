package app.user.model;

public enum UserCountry {
    BULGARIA("Bulgaria"),
    GERMANY("Germany"),
    FRANCE("France");

    private final String displayName;

    UserCountry(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
