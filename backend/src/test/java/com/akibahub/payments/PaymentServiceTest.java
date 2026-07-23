package com.akibahub.payments;

import com.akibahub.audit.AuditLogService;
import com.akibahub.payments.dto.PaymentStatusResponse;
import com.akibahub.payments.entity.PendingPayment;
import com.akibahub.payments.entity.PendingPaymentRepository;
import com.akibahub.shared.exception.BadRequestException;
import com.akibahub.shared.exception.ConflictException;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.WalletService;
import com.akibahub.wallet.dto.WalletResponse;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PendingPaymentRepository pendingRepo;
    @Mock WalletRepository walletRepo;
    @Mock WalletService walletService;
    @Mock AuditLogService auditLog;

    PaymentService paymentService;

    User user;
    Wallet wallet;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                pendingRepo, walletRepo, walletService, auditLog, "demo", 120);
        user = User.builder().id(1L).phoneNumber("254712345678").fullName("Test").idNumber("12345678")
                .passwordHash("x").build();
        wallet = Wallet.builder().id(10L).type(Wallet.WalletType.PERSONAL).balance(new BigDecimal("100.00"))
                .user(user).version(0L).build();
    }

    @Test
    void initiateDeposit_doesNotCreditWallet() {
        when(walletRepo.findByUserIdAndType(1L, Wallet.WalletType.PERSONAL)).thenReturn(Optional.of(wallet));
        when(pendingRepo.save(any(PendingPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentStatusResponse result = paymentService.initiateDeposit(user, new BigDecimal("50.00"), null);

        assertEquals("PENDING", result.status());
        assertEquals(new BigDecimal("50.00"), result.amount());
        assertNotNull(result.reference());
        assertNull(result.balance());
        verify(walletService, never()).creditPersonalDeposit(any(), any(), any());
        verify(pendingRepo).save(any(PendingPayment.class));
    }

    @Test
    void completeDemo_creditsOnce() {
        PendingPayment pending = PendingPayment.builder()
                .id(1L)
                .reference("AH-TESTREF1234567")
                .user(user)
                .wallet(wallet)
                .amount(new BigDecimal("50.00"))
                .phone(user.getPhoneNumber())
                .status(PendingPayment.Status.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(2))
                .createdAt(LocalDateTime.now())
                .build();

        when(pendingRepo.findByReferenceAndUser_Id("AH-TESTREF1234567", 1L)).thenReturn(Optional.of(pending));
        when(walletService.creditPersonalDeposit(eq(user), eq(new BigDecimal("50.00")), eq("AH-TESTREF1234567")))
                .thenReturn(new WalletResponse(10L, Wallet.WalletType.PERSONAL, new BigDecimal("150.00"),
                        1L, 1L, null, null));
        when(pendingRepo.save(any(PendingPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentStatusResponse first = paymentService.completeDemo(user, "AH-TESTREF1234567");
        assertEquals("COMPLETED", first.status());
        assertEquals(new BigDecimal("150.00"), first.balance());
        verify(walletService, times(1)).creditPersonalDeposit(any(), any(), any());

        PaymentStatusResponse second = paymentService.completeDemo(user, "AH-TESTREF1234567");
        assertEquals("COMPLETED", second.status());
        verify(walletService, times(1)).creditPersonalDeposit(any(), any(), any());
    }

    @Test
    void completeDemo_disabledInLiveMode() {
        paymentService = new PaymentService(
                pendingRepo, walletRepo, walletService, auditLog, "live", 120);
        assertThrows(ForbiddenException.class,
                () -> paymentService.completeDemo(user, "AH-ANY"));
        verify(walletService, never()).creditPersonalDeposit(any(), any(), any());
    }

    @Test
    void initiateDeposit_rejectsNonPositiveAmount() {
        assertThrows(BadRequestException.class,
                () -> paymentService.initiateDeposit(user, BigDecimal.ZERO, null));
        verify(pendingRepo, never()).save(any());
    }

    @Test
    void completeDemo_rejectsExpiredPending() {
        PendingPayment pending = PendingPayment.builder()
                .reference("AH-EXPIRED0000001")
                .user(user)
                .wallet(wallet)
                .amount(new BigDecimal("10.00"))
                .status(PendingPayment.Status.PENDING)
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();
        when(pendingRepo.findByReferenceAndUser_Id("AH-EXPIRED0000001", 1L)).thenReturn(Optional.of(pending));
        when(pendingRepo.save(any(PendingPayment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(ConflictException.class,
                () -> paymentService.completeDemo(user, "AH-EXPIRED0000001"));

        ArgumentCaptor<PendingPayment> captor = ArgumentCaptor.forClass(PendingPayment.class);
        verify(pendingRepo).save(captor.capture());
        assertEquals(PendingPayment.Status.EXPIRED, captor.getValue().getStatus());
        verify(walletService, never()).creditPersonalDeposit(any(), any(), any());
    }
}
