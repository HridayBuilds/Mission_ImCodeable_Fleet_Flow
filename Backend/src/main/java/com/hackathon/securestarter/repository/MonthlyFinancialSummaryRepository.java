package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.MonthlyFinancialSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyFinancialSummaryRepository extends JpaRepository<MonthlyFinancialSummary, UUID> {

    Optional<MonthlyFinancialSummary> findByYearAndMonth(Integer year, Integer month);

    List<MonthlyFinancialSummary> findByYearOrderByMonthAsc(Integer year);

    List<MonthlyFinancialSummary> findAllByOrderByYearDescMonthDesc();

}
