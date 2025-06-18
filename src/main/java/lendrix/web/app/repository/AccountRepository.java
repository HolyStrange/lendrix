package lendrix.web.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import lendrix.web.app.entity.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

}
