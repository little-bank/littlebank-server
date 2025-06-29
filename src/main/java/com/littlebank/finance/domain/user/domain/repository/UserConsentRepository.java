package com.littlebank.finance.domain.user.domain.repository;

import com.littlebank.finance.domain.user.domain.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {
}
