package lendrix.web.app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.naming.OperationNotSupportedException;

import org.springframework.stereotype.Service;

import lendrix.web.app.entity.Account;
import lendrix.web.app.entity.Card;
import lendrix.web.app.entity.Transaction;
import lendrix.web.app.entity.User;
import lendrix.web.app.enums.Status;
import lendrix.web.app.enums.Type;
import lendrix.web.app.repository.AccountRepository;
import lendrix.web.app.repository.CardRepository;
import lendrix.web.app.repository.TransactionRepository;
import lendrix.web.app.service.helper.AccountHelper;
import lendrix.web.app.util.RandomUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final AccountHelper accountHelper;

    public Card createCard(BigDecimal amount, User user, String billingAddress, int pin)
            throws OperationNotSupportedException {


        if (amount == null || amount.compareTo(BigDecimal.valueOf(2)) < 0) {
            throw new IllegalArgumentException("Amount must be at least 2");
        }

        String userUid = user.getUid();

        if (!accountRepository.existsByCodeAndOwnerUid("USD", userUid)) {
            throw new IllegalArgumentException("No USD account found for user");
        }

        Account usdAccount = accountRepository.findByCodeAndOwnerUid("USD", userUid).orElseThrow();

        accountHelper.validateSufficientFunds(usdAccount, amount);

        usdAccount.setBalance(usdAccount.getBalance().subtract(amount));
        accountRepository.save(usdAccount);

        accountHelper.createAccountTransaction(
                1,
                Type.WITHDRAW,
                BigDecimal.ZERO,
                user,
                usdAccount,
                amount,
                "Card creation fee"
        );

        String cardNumber;
        do {
            cardNumber = generateCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));

        LocalDateTime exp = LocalDateTime.now().plusYears(3);

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .cardHolder(user.getFirstname() + " " + user.getLastname())
                .balance(amount)
                .owner(user)
                .billingAddress(billingAddress)
                .pin(pin)
                .cvv((int) new RandomUtil().generateRandom(3))
                .exp(exp)
                .build();

        cardRepository.save(card);

        accountHelper.createAccountTransaction(
                1,
                Type.CREDIT,
                BigDecimal.ZERO,
                user,
                usdAccount,
                amount,
                "Card funded"
        );

        createCardTransaction(amount, user, card, BigDecimal.ZERO, Type.CREDIT);

        return card;
    }

    private String generateCardNumber() {
        return String.valueOf(new RandomUtil().generateRandom(16));
    }

    public Transaction creditCard(BigDecimal amount, User user) {
        Card card = getCard(user);
        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);

        Account usdAccount = accountRepository.findByCodeAndOwnerUid("USD", user.getUid()).orElseThrow();
        usdAccount.setBalance(usdAccount.getBalance().add(amount));
        accountRepository.save(usdAccount);

        accountHelper.createAccountTransaction(
                1,
                Type.CREDIT,
                BigDecimal.ZERO,
                user,
                usdAccount,
                amount,
                "Card credited"
        );

        return createCardTransaction(amount, user, card, BigDecimal.ZERO, Type.CREDIT);
    }

    public Transaction debitCard(BigDecimal amount, User user) {
        Card card = getCard(user);
        card.setBalance(card.getBalance().subtract(amount));
        cardRepository.save(card);

        Account usdAccount = accountRepository.findByCodeAndOwnerUid("USD", user.getUid()).orElseThrow();
        usdAccount.setBalance(usdAccount.getBalance().subtract(amount));
        accountRepository.save(usdAccount);

        accountHelper.createAccountTransaction(
                1,
                Type.WITHDRAW,
                BigDecimal.ZERO,
                user,
                usdAccount,
                amount,
                "Card debited"
        );

        return createCardTransaction(amount, user, card, BigDecimal.ZERO, Type.WITHDRAW);
    }

    private Transaction createCardTransaction(BigDecimal amount, User user, Card card, BigDecimal txFee, Type type) {
        Transaction tx = Transaction.builder()
                .card(card)
                .owner(user)
                .amount(amount)
                .txFee(txFee)
                .status(Status.COMPLETED)
                .type(type)
                .build();
        return transactionRepository.save(tx);
    }

    public Card getCard(User user) {
        return cardRepository.findByOwnerUid(user.getUid()).orElseThrow();
    }
}
