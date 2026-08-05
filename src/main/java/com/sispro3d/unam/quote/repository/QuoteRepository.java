package com.sispro3d.unam.quote.repository;

import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.user.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByClient(Account client);
    List<Quote> findByOfferedService(OfferedService offeredService);
    List<Quote> findByStatus(QuoteStatus status);

    List<Quote> findByClient_IdUser(Long clientId);
    List<Quote> findByOfferedService_Id(Long offeredServiceId);
    List<Quote> findByOfferedService_Expert_IdUser(Long expertId);
}
