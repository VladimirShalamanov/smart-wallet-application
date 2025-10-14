package app.subscription.model;

public enum SubscriptionPeriod {
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    private final String displayName;

    SubscriptionPeriod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
