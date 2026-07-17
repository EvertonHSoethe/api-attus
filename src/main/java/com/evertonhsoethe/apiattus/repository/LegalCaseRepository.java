package com.evertonhsoethe.apiattus.repository;

import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import com.evertonhsoethe.apiattus.enums.CaseTypeEnum;
import com.evertonhsoethe.apiattus.domain.LegalCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {

    Optional<LegalCase> findByCaseNumber(String caseNumber);

    boolean existsByCaseNumber(String caseNumber);

    List<LegalCase> findByStatus(CaseStatusEnum status);

    List<LegalCase> findByType(CaseTypeEnum type);
}
