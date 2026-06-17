package com.clinica.mariana.restms.financial.repository;

import com.clinica.mariana.restms.financial.entity.FinancialTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransactionEntity, UUID> {

	List<FinancialTransactionEntity> findAllByClinicIdAndStatusNotOrderByTransactionDateDescCreatedAtDesc(
			UUID clinicId, String status);

	List<FinancialTransactionEntity> findAllByClinicIdAndStatusNotAndTransactionDateBetweenOrderByTransactionDateDesc(
			UUID clinicId, String status, LocalDate start, LocalDate end);

	@Query("""
			SELECT EXTRACT(YEAR  FROM f.transactionDate) AS year,
				   EXTRACT(MONTH FROM f.transactionDate) AS month,
				   f.type,
				   SUM(f.amount) AS total
			FROM FinancialTransactionEntity f
			WHERE f.clinicId = :clinicId
			  AND f.transactionDate >= :start
			  AND f.transactionDate <= :end
			  AND f.status <> 'CANCELLED'
			GROUP BY EXTRACT(YEAR FROM f.transactionDate), EXTRACT(MONTH FROM f.transactionDate), f.type
			ORDER BY EXTRACT(YEAR FROM f.transactionDate) ASC, EXTRACT(MONTH FROM f.transactionDate) ASC
			""")
	List<Object[]> findMonthlyTrend(@Param("clinicId") UUID clinicId, @Param("start") LocalDate start,
			@Param("end") LocalDate end);

	@Query("""
			SELECT f.category, SUM(f.amount)
			FROM FinancialTransactionEntity f
			WHERE f.clinicId = :clinicId
			  AND f.type = 'RECEITA'
			  AND f.status <> 'CANCELLED'
			  AND f.transactionDate >= :start
			  AND f.transactionDate <= :end
			GROUP BY f.category
			ORDER BY SUM(f.amount) DESC
			""")
	List<Object[]> findRevenueByCategory(@Param("clinicId") UUID clinicId, @Param("start") LocalDate start,
			@Param("end") LocalDate end);
}
