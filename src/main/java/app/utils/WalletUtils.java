package app.utils;

import app.subscription.model.SubscriptionType;
import app.user.model.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WalletUtils {

    public static boolean isEligibleToUnlockNewWallet(User user) {

        // DEFAULT - ineligible to unlock new Wallet

        // When can we unlock a new Wallet?
        // PREMIUM && Wallets are less than 2
        // ULTIMATE && Wallets are less than 3

        SubscriptionType subscriptionType = user.getSubscriptions().get(0).getType();
        int walletsSize = user.getWallets().size();

        return (subscriptionType == SubscriptionType.PREMIUM && walletsSize < 2)
                || (subscriptionType == SubscriptionType.ULTIMATE && walletsSize < 3);
    }
}
