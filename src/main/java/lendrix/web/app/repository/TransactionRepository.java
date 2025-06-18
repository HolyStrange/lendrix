package lendrix.web.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import lendrix.web.app.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

}
