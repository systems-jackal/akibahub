package com.akibahub.payments;

import com.akibahub.audit.AuditLogService;
import com.akibahub.payments.dto.PaymentStatusResponse;
import com.akibahub.payments.entity.PendingPayment;
import com.akibahub.payments.entity.PendingPaymentRepository;
import com.akibahub.shared.AmountValidator;
import com.akibahub.shared.exception.BadRequestException;
import com.akibahub.shared.exception.ConflictException;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.shared.exception.NotFoundException;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.WalletService;
import com.akibahub.wallet.dto.WalletResponse;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PendingPaymentRepository pendingRepo;
    private final WalletRepository walletRepo;
    private final WalletService walletService;
    private final AuditLogService auditLog;
    private final String paymentsMode;
    private final long pendingTtlSeconds;

    public PaymentService(PendingPaymentRepository pendingRepo,
                          WalletRepository walletRepo,
                          WalletService walletService,
                          AuditLogService auditLog,
                          @Value("${payments.mode:demo}") String paymentsMode,
                          @Value("${payments.pending-ttl-seconds:120}") long pendingTtlSeconds) {
        this.pendingRepo = pendingRepo;
        this.walletRepo = walletRepo;
        this.walletService = walletService;
        this.auditLog = auditLog;
        this.paymentsMode = paymentsMode;
        this.pendingTtlSeconds = pendingTtlSeconds;
    }

    public boolean isDemoMode() {
        return !"live".equalsIgnoreCase(paymentsMode);
    }

    /**
     * Starts an STK-style deposit. Does not credit the wallet — credit
     * happens only after demo-complete or a verified PayHero IPN.
     */
    @Transactional
    public PaymentStatusResponse initiateDeposit(User user, BigDecimal amount, String phone) {
        AmountValidator.requirePositive(amount);
        Wallet wallet = walletRepo.findByUserIdAndType(user.getId(), Wallet.WalletType.PERSONAL)
                .orElseThrow(() -> new NotFoundException("Personal wallet not found"));

        String msisdn = (phone == null || phone.isBlank()) ? user.getPhoneNumber() : phone.trim();
        String reference = "AH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        PendingPayment pending = PendingPayment.builder()
                .reference(reference)
                .user(user)
                .wallet(wallet)
                .amount(amount)
                .phone(msisdn)
                .status(PendingPayment.Status.PENDING)
                .expiresAt(LocalDateTime.now().plusSeconds(pendingTtlSeconds))
                .build();
        pendingRepo.save(pending);

        auditLog.logEvent("DEPOSIT_INITIATED", Map.of(
                "user", user.getPhoneNumber(),
                "amount", amount,
                "reference", reference,
                "mode", paymentsMode
        ));

        return PaymentStatusResponse.from(pending);
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponse getStatus(User user, String reference) {
        PendingPayment payment = requireOwned(user, reference);
        expireIfNeeded(payment);
        BigDecimal balance = null;
        if (payment.getStatus() == PendingPayment.Status.COMPLETED) {
            balance = payment.getWallet().getBalance();
        }
        return PaymentStatusResponse.from(payment, balance);
    }

    /**
     * Presentation-only: simulates a successful PayHero/M-Pesa callback.
     * Credits the wallet exactly once for the given reference.
     */
    @Transactional
    public PaymentStatusResponse completeDemo(User user, String reference) {
        if (!isDemoMode()) {
            throw new ForbiddenException("Demo payment completion is disabled when payments.mode=live");
        }
        if (reference == null || reference.isBlank()) {
            throw new BadRequestException("Payment reference is required");
        }

        PendingPayment payment = requireOwned(user, reference);
        expireIfNeeded(payment);

        if (payment.getStatus() == PendingPayment.Status.COMPLETED) {
            return PaymentStatusResponse.from(payment, payment.getWallet().getBalance());
        }
        if (payment.getStatus() != PendingPayment.Status.PENDING) {
            throw new ConflictException("Payment is " + payment.getStatus() + " and cannot be completed");
        }

        WalletResponse credited = walletService.creditPersonalDeposit(
                user, payment.getAmount(), payment.getReference());

        payment.setStatus(PendingPayment.Status.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        pendingRepo.save(payment);

        auditLog.logEvent("DEPOSIT_DEMO_COMPLETED", Map.of(
                "user", user.getPhoneNumber(),
                "amount", payment.getAmount(),
                "reference", payment.getReference()
        ));

        return PaymentStatusResponse.from(payment, credited.balance());
    }

    private PendingPayment requireOwned(User user, String reference) {
        return pendingRepo.findByReferenceAndUser_Id(reference, user.getId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }

    private void expireIfNeeded(PendingPayment payment) {
        if (payment.getStatus() == PendingPayment.Status.PENDING
                && payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus(PendingPayment.Status.EXPIRED);
            pendingRepo.save(payment);
        }
    }
}
