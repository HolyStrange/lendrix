package lendrix.web.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lendrix.web.app.dto.AccountDto;
import lendrix.web.app.dto.ConvertDto;
import lendrix.web.app.dto.TransferDto;
import lendrix.web.app.entity.Account;
import lendrix.web.app.entity.Transaction;
import lendrix.web.app.entity.User;
import lendrix.web.app.service.AccountService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody AccountDto accountDto, Authentication authentication) {
        var user = (User) authentication.getPrincipal();

        try {
            Account account = accountService.createAccount(accountDto, user);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Account>> getUserAccounts(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(accountService.getUserAccounts(user.getUid()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transferFunds(@RequestBody TransferDto transferDto,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();

        try {
            Transaction transaction = accountService.transferFunds(transferDto, user);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @GetMapping("/rates")
    public ResponseEntity<Map<String, Double>> getExchangeRate() {
        return ResponseEntity.ok(accountService.getExchangeRate());
    }
    
    @PostMapping("/convert")
    public ResponseEntity<Transaction> convertCurrency(@RequestBody ConvertDto convertDto,
            Authentication authentication) throws Exception {
        var user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(accountService.convertCurrency(convertDto, user));
    }

}




