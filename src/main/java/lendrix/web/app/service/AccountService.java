package lendrix.web.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lendrix.web.app.dto.AccountDto;
import lendrix.web.app.dto.ConvertDto;
import lendrix.web.app.dto.TransferDto;
import lendrix.web.app.entity.Account;
import lendrix.web.app.entity.Transaction;
import lendrix.web.app.entity.User;
import lendrix.web.app.repository.AccountRepository;
import lendrix.web.app.service.helper.AccountHelper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountHelper accountHelper;
    private final ExchangeRateService exchangeRateService;

    public Account createAccount(AccountDto accountDto, User user) throws Exception {
        return accountHelper.createAccount(accountDto, user);
    }

    public List<Account> getUserAccounts(String uid) {
        return accountRepository.findAllByOwnerUid(uid);
    }

    public Transaction transferFunds(TransferDto transferDto, User user) {
        // Get sender account using code and user ID
        var senderAccount = accountRepository
                .findByCodeAndOwnerUid(transferDto.getSenderAccountCode(), user.getUid())
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

        // Convert recipientAccountNumber from String to long
        long recipientAccountNumber;
        try {
            recipientAccountNumber = Long.parseLong(transferDto.getRecipientAccountNumber());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Recipient account number must be numeric");
        }

        // Get receiver account using account number
        var receiverAccount = accountRepository
                .findByAccountNumber(recipientAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

        // Perform transfer
        return accountHelper.performTransfer(senderAccount, receiverAccount, transferDto.getAmount(), user);
    }
    
    public Map<String, Double> getExchangeRate() {
        return exchangeRateService.getRates();
    }

    public Transaction convertCurrency(ConvertDto convertDto, User user) throws Exception {
        return accountHelper.convertCurrency(convertDto, user);
    }
}
