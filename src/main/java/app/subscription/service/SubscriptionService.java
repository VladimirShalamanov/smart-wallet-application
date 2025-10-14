package app.subscription.service;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionRepository;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.user.model.User;
import app.wallet.service.WalletService;
import app.web.dto.UpgradeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WalletService walletService;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, WalletService walletService) {
        this.subscriptionRepository = subscriptionRepository;
        this.walletService = walletService;
    }

    public Subscription createDefaultSubscription(User user) {

        Subscription subscription = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(LocalDateTime.now())
                .expiryOn(LocalDateTime.now().plusMonths(1))
                .build();

        return subscriptionRepository.save(subscription);
    }

    public Transaction upgrade(User user, @Valid UpgradeRequest upgradeRequest, SubscriptionType subscriptionType) {

        Optional<Subscription> currentlyActiveSubscriptionOpt = subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId());

        if (currentlyActiveSubscriptionOpt.isEmpty()) {
            throw new RuntimeException("No active subscription was found for user with id [%s]".formatted(user.getId()));
        }

        BigDecimal subscriptionPrice = getUpgradePrice(subscriptionType, upgradeRequest.getPeriod());
        String chargeDescription = "Upgrade request for %s %s".formatted(upgradeRequest.getPeriod().getDisplayName(), subscriptionType);

        Transaction chargeResultTransaction = walletService.withdrawal(user, upgradeRequest.getWalletId(), subscriptionPrice, chargeDescription);
        if (chargeResultTransaction.getStatus() == TransactionStatus.FAILED) {
            return chargeResultTransaction;
        }

        Subscription currentlyActiveSubscription = currentlyActiveSubscriptionOpt.get();
        LocalDateTime now = LocalDateTime.now();

        Subscription newActiveSubscription = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(upgradeRequest.getPeriod())
                .type(subscriptionType)
                .price(subscriptionPrice)
                .renewalAllowed(upgradeRequest.getPeriod() == SubscriptionPeriod.MONTHLY)
                .createdOn(now)
                .expiryOn(upgradeRequest.getPeriod() == SubscriptionPeriod.MONTHLY
                        ? now.plusMonths(1).truncatedTo(ChronoUnit.DAYS)
                        : now.plusYears(1).truncatedTo(ChronoUnit.DAYS))
                .build();

        currentlyActiveSubscription.setStatus(SubscriptionStatus.COMPLETED);
        currentlyActiveSubscription.setExpiryOn(now);

        subscriptionRepository.save(currentlyActiveSubscription);
        subscriptionRepository.save(newActiveSubscription);

        return chargeResultTransaction;
    }

    private BigDecimal getUpgradePrice(SubscriptionType type, SubscriptionPeriod period) {

        if (type == SubscriptionType.DEFAULT) {
            return BigDecimal.ZERO;

        } else if (type == SubscriptionType.PREMIUM && period == SubscriptionPeriod.MONTHLY) {
            return new BigDecimal("19.99");
        } else if (type == SubscriptionType.PREMIUM && period == SubscriptionPeriod.YEARLY) {
            return new BigDecimal("199.99");

        } else if (type == SubscriptionType.ULTIMATE && period == SubscriptionPeriod.MONTHLY) {
            return new BigDecimal("49.99");
        } else if (type == SubscriptionType.ULTIMATE && period == SubscriptionPeriod.YEARLY) {
            return new BigDecimal("499.99");
        }

        throw new RuntimeException("Price not found for type [%s] and period [%s]".formatted(type, period));
    }
}
