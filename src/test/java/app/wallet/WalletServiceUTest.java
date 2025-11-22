package app.wallet;

import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class WalletServiceUTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WalletService walletService;

    private User createUser(UUID id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@mail.com")
                .username("user" + id)
                .build();
    }

    private Wallet createWallet(UUID id, User owner, BigDecimal balance, WalletStatus status) {
        return Wallet.builder()
                .id(id)
                .owner(owner)
                .balance(balance)
                .status(status)
                .currency(Currency.getInstance("EUR"))
                .build();
    }

    // ----------------------------------------------------------------------------------------
    // TEST 1 – Wallet inactive → FAILED
    // ----------------------------------------------------------------------------------------
    @Test
    void withdrawal_fails_whenWalletInactive() {

        UUID userId = UUID.randomUUID();

        User user = createUser(userId);
        Wallet wallet = createWallet(UUID.randomUUID(), user, new BigDecimal("100.00"), WalletStatus.INACTIVE);

        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        when(transactionService.upsert(any())).thenAnswer(i -> i.getArgument(0));

        Transaction tx = walletService.withdrawal(user, wallet.getId(), new BigDecimal("10.00"), "Test");

        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(tx.getFailureReason()).isEqualTo("Inactive wallet");

        verify(walletRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(transactionService).upsert(tx);
    }
}
