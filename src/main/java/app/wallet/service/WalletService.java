package app.wallet.service;

import app.email.EmailService;
import app.event.SuccessfulChargeEvent;
import app.gift.GiftService;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.utils.WalletUtils;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.web.dto.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WalletService {

    private static final String SMART_WALLET_IDENTIFIER = "SMART WALLET PLATFORM";
    private static final String INACTIVE_WALLET_FAILURE_REASON = "Inactive wallet";
    private static final String INSUFFICIENT_FUNDS_FAILURE_REASON = "Not enough funds";
    private static final String WALLET_NOT_OWNED_FAILURE_REASON = "You don't own this wallet";
    private static final String TOP_UP_DESCRIPTION_FORMAT = "Top-up %.2f";
    private static final String TRANSFER_DESCRIPTION_FORMAT = "Transfer %s <> %s (%.2f)";

    private static final String FIRST_WALLET_NICKNAME = "Vault Zero";
    private static final String SECOND_WALLET_NICKNAME = "Nova Flow";
    private static final String THIRD_WALLET_NICKNAME = "Pulse Pay";

    private static final BigDecimal INITIAL_WALLET_BALANCE = new BigDecimal("20.00");
    private static final Currency DEFAULT_WALLET_CURRENCY = Currency.getInstance("EUR");

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    private final ApplicationEventPublisher eventPublisher;

    private final EmailService emailService;
    private final GiftService giftService;

    @Autowired
    public WalletService(WalletRepository walletRepository,
                         TransactionService transactionService,
                         ApplicationEventPublisher eventPublisher,
                         EmailService emailService,
                         GiftService giftService) {

        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
        this.giftService = giftService;
    }

    @Transactional
    public Transaction withdrawal(User user, UUID walletId, BigDecimal amount, String description) {

        Wallet wallet = getById(walletId);

        Transaction transaction = Transaction.builder()
                .owner(user)
                .sender(wallet.getId().toString())
                .receiver(SMART_WALLET_IDENTIFIER)
                .amount(amount)
                .currency(wallet.getCurrency())
                .type(TransactionType.WITHDRAWAL)
                .description(description)
                .createdOn(LocalDateTime.now())
                .build();

        if (!isActiveWallet(wallet)) {
            transaction.setFailureReason(INACTIVE_WALLET_FAILURE_REASON);
            transaction.setStatus(TransactionStatus.FAILED);

        } else if (!hasSufficientFunds(wallet, amount)) {
            transaction.setFailureReason(INSUFFICIENT_FUNDS_FAILURE_REASON);
            transaction.setStatus(TransactionStatus.FAILED);

        } else if (!isWalletOwnedByUser(wallet, user)) {
            transaction.setFailureReason(WALLET_NOT_OWNED_FAILURE_REASON);
            transaction.setStatus(TransactionStatus.FAILED);

        } else {
            transaction.setStatus(TransactionStatus.SUCCEEDED);
            wallet.setBalance(wallet.getBalance().subtract(amount));
            wallet.setUpdatedOn(LocalDateTime.now());
            walletRepository.save(wallet);

            SuccessfulChargeEvent event = SuccessfulChargeEvent.builder()
                    .userId(user.getId())
                    .walletId(walletId)
                    .amount(amount)
                    .email(user.getEmail())
                    .createdOn(LocalDateTime.now())
                    .build();
            eventPublisher.publishEvent(event);
        }

        transaction.setBalanceLeft(wallet.getBalance());

        return transactionService.upsert(transaction);
    }

    public boolean isWalletOwnedByUser(Wallet wallet, User user) {
        return wallet.getOwner().getId().equals(user.getId());
    }

    public boolean isActiveWallet(Wallet wallet) {
        return wallet.getStatus() == WalletStatus.ACTIVE;
    }

    public boolean hasSufficientFunds(Wallet wallet, BigDecimal amount) {
        return wallet.getBalance().compareTo(amount) > 0;
    }

    @Transactional
    public Transaction deposit(UUID walletId, BigDecimal topUpAmount, String description) {

        Wallet wallet = getById(walletId);

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            return transactionService.createNewTransaction(
                    wallet.getOwner(),
                    SMART_WALLET_IDENTIFIER,
                    wallet.getId().toString(),
                    topUpAmount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.DEPOSIT,
                    TransactionStatus.FAILED,
                    description,
                    INACTIVE_WALLET_FAILURE_REASON);
        }

        wallet.setBalance(wallet.getBalance().add(topUpAmount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

        return transactionService.createNewTransaction(
                wallet.getOwner(),
                SMART_WALLET_IDENTIFIER,
                wallet.getId().toString(),
                topUpAmount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                description,
                null);
    }

    public Wallet createDefaultWallet(User user) {

        Wallet wallet = Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .nickname(FIRST_WALLET_NICKNAME)
                .balance(INITIAL_WALLET_BALANCE)
                .currency(DEFAULT_WALLET_CURRENCY)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .main(true)
                .build();

        return walletRepository.save(wallet);
    }

    private Wallet getById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet by id [%s] was not found.".formatted(walletId)));
    }

    @Transactional
    public Transaction transfer(TransferRequest transferRequest) {

        Wallet senderWallet = getById(transferRequest.getWalletId());
        Wallet receiverWallet = getPrimaryByUsername(transferRequest.getRecipientUsername());

        String transferDescription = TRANSFER_DESCRIPTION_FORMAT.formatted(senderWallet.getOwner().getUsername(), receiverWallet.getOwner().getUsername(), transferRequest.getAmount());
        Transaction withdrawalTransaction = withdrawal(senderWallet.getOwner(), senderWallet.getId(), transferRequest.getAmount(), transferDescription);

        if (withdrawalTransaction.getStatus() == TransactionStatus.SUCCEEDED) {
            deposit(receiverWallet.getId(), transferRequest.getAmount(), transferDescription);
        }

        return withdrawalTransaction;
    }

    private Wallet getPrimaryByUsername(String recipientUsername) {

        return walletRepository.findByOwnerUsername(recipientUsername)
                .stream()
                .filter(Wallet::isMain)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("[%s] doesn't have any primary wallets.".formatted(recipientUsername)));
    }

    @Transactional
    public Transaction topUp(UUID walletId) {

        BigDecimal amount = new BigDecimal("20.00");

        return deposit(walletId, amount, TOP_UP_DESCRIPTION_FORMAT.formatted(amount));
    }

    public void changeStatus(UUID walletId) {

        Wallet wallet = getById(walletId);

        if (wallet.getStatus() == WalletStatus.ACTIVE) {
            if (wallet.isMain()) {
                throw new RuntimeException("Primary wallets can't be inactive!");
            }
            wallet.setStatus(WalletStatus.INACTIVE);
        } else {
            wallet.setStatus(WalletStatus.ACTIVE);
        }

        walletRepository.save(wallet);
    }

    @Transactional
    public void promoteToPrimary(UUID walletId) {

        Wallet wallet = getById(walletId);

        if (wallet.isMain()) {
            throw new RuntimeException("This wallet is already primary!");
        }

        User owner = wallet.getOwner();
        Optional<Wallet> currentPrimaryWalletOpt = walletRepository.findByOwner_IdAndMain(owner.getId(), true);

        if (currentPrimaryWalletOpt.isPresent()) {
            Wallet currentPrimaryWallet = currentPrimaryWalletOpt.get();
            currentPrimaryWallet.setMain(false);
            currentPrimaryWallet.setUpdatedOn(LocalDateTime.now());
            walletRepository.save(currentPrimaryWallet);
        }

        wallet.setMain(true);
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    public void unlockNewWallet(User user) {

        boolean isEligibleToUnlock = WalletUtils.isEligibleToUnlockNewWallet(user);
        if (!isEligibleToUnlock) {
            throw new RuntimeException("This user reached the max number of allowed wallets.");
        }

        Wallet newWallet = Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .nickname(user.getWallets().size() == 1 ? SECOND_WALLET_NICKNAME : THIRD_WALLET_NICKNAME)
                .balance(BigDecimal.ZERO)
                .currency(DEFAULT_WALLET_CURRENCY)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .main(false)
                .build();

        walletRepository.save(newWallet);
    }

    public Map<UUID, List<Transaction>> getLastFourTransactions(List<Wallet> wallets) {

        Map<UUID, List<Transaction>> transactionsByWalletId = new HashMap<>();

        for (Wallet wallet : wallets) {

            List<Transaction> lastFourTransactions = transactionService.getLastFourTransactions(wallet);
            transactionsByWalletId.put(wallet.getId(), lastFourTransactions);
        }

        return transactionsByWalletId;
    }
}
