package lendrix.web.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lendrix.web.app.dto.CardDto;
import lendrix.web.app.dto.AmountDto;
import lendrix.web.app.entity.Card;
import lendrix.web.app.entity.Transaction;
import lendrix.web.app.entity.User;
import lendrix.web.app.service.CardService;
import lombok.RequiredArgsConstructor;

import javax.naming.OperationNotSupportedException;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Card> getCard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.getCard(user));
    }

    @PostMapping("/create")
    public ResponseEntity<Card> createCard(
            @RequestBody CardDto cardDto,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(cardService.createCard(
                    cardDto.getAmount(),
                    user,
                    cardDto.getBillingAddress(),
                    cardDto.getPin()
            ));
        } catch (OperationNotSupportedException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/credit")
    public ResponseEntity<Transaction> creditCard(
            @RequestBody AmountDto amountDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.creditCard(amountDto.getAmount(), user));
    }

    @PostMapping("/debit")
    public ResponseEntity<Transaction> debitCard(
            @RequestBody AmountDto amountDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.debitCard(amountDto.getAmount(), user));
    }
}
