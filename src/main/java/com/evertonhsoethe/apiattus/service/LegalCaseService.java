package com.evertonhsoethe.apiattus.service;

import com.evertonhsoethe.apiattus.domain.LegalCase;
import com.evertonhsoethe.apiattus.dto.LegalCaseRequestDto;
import com.evertonhsoethe.apiattus.dto.LegalCaseResponseDto;
import com.evertonhsoethe.apiattus.dto.PatchLegalCaseRequestDto;
import com.evertonhsoethe.apiattus.dto.UpdateStatusRequestDto;
import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import com.evertonhsoethe.apiattus.enums.CaseTypeEnum;
import com.evertonhsoethe.apiattus.exception.CaseNotFoundException;
import com.evertonhsoethe.apiattus.exception.DuplicateCaseNumberException;
import com.evertonhsoethe.apiattus.mapper.LegalCaseMapper;
import com.evertonhsoethe.apiattus.repository.LegalCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalCaseService {

    private static final Set<CaseStatusEnum> TERMINAL_STATUSES = Set.of(CaseStatusEnum.CLOSED, CaseStatusEnum.ARCHIVED);

    private final LegalCaseRepository repository;
    private final LegalCaseMapper mapper;

    @Transactional
    public LegalCaseResponseDto create(LegalCaseRequestDto request) {
        if (repository.existsByCaseNumber(request.caseNumber())) {
            log.warn("Attempt to create duplicate case number: {}", request.caseNumber());
            throw new DuplicateCaseNumberException(request.caseNumber());
        }

        LegalCase legalCase = mapper.toEntity(request);
        if (legalCase.getStatus() == null) {
            legalCase.setStatus(CaseStatusEnum.OPEN);
        }

        LegalCase saved = repository.save(legalCase);
        log.info("Legal case created [id={}, caseNumber={}]", saved.getId(), saved.getCaseNumber());
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public LegalCaseResponseDto findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new CaseNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public LegalCaseResponseDto findByCaseNumber(String caseNumber) {
        return repository.findByCaseNumber(caseNumber)
                .map(mapper::toDto)
                .orElseThrow(() -> new CaseNotFoundException(caseNumber));
    }

    @Transactional(readOnly = true)
    public List<LegalCaseResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LegalCaseResponseDto> findByStatus(CaseStatusEnum status) {
        return repository.findByStatus(status).stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LegalCaseResponseDto> findByType(CaseTypeEnum type) {
        return repository.findByType(type).stream().map(mapper::toDto).toList();
    }

    @Transactional
    public LegalCaseResponseDto update(Long id, LegalCaseRequestDto request) {
        LegalCase legalCase = repository.findById(id)
                .orElseThrow(() -> new CaseNotFoundException(id));

        if (!legalCase.getCaseNumber().equals(request.caseNumber())
                && repository.existsByCaseNumber(request.caseNumber())) {
            log.warn("Case number already in use: {}", request.caseNumber());
            throw new DuplicateCaseNumberException(request.caseNumber());
        }

        mapper.updateEntity(request, legalCase);
        if (request.status() != null) {
            legalCase.setStatus(request.status());
        }

        LegalCase updated = repository.save(legalCase);
        log.info("Legal case updated [id={}]", updated.getId());
        return mapper.toDto(updated);
    }

    @Transactional
    public LegalCaseResponseDto patch(Long id, PatchLegalCaseRequestDto request) {
        LegalCase legalCase = repository.findById(id)
                .orElseThrow(() -> new CaseNotFoundException(id));

        if (request.caseNumber() != null
                && !request.caseNumber().equals(legalCase.getCaseNumber())
                && repository.existsByCaseNumber(request.caseNumber())) {
            log.warn("Case number already in use: {}", request.caseNumber());
            throw new DuplicateCaseNumberException(request.caseNumber());
        }

        mapper.patchEntity(request, legalCase);

        if (request.status() != null) {
            if (TERMINAL_STATUSES.contains(request.status())) {
                if (legalCase.getClosingDate() == null) {
                    legalCase.setClosingDate(LocalDate.now());
                }
            } else {
                legalCase.setClosingDate(null);
            }
        }

        LegalCase updated = repository.save(legalCase);
        log.info("Legal case patched [id={}]", updated.getId());
        return mapper.toDto(updated);
    }

    @Transactional
    public LegalCaseResponseDto updateStatus(Long id, UpdateStatusRequestDto request) {
        LegalCase legalCase = repository.findById(id)
                .orElseThrow(() -> new CaseNotFoundException(id));

        CaseStatusEnum previous = legalCase.getStatus();
        legalCase.setStatus(request.status());

        if (TERMINAL_STATUSES.contains(request.status())) {
            if (legalCase.getClosingDate() == null) {
                legalCase.setClosingDate(LocalDate.now());
            }
        } else {
            legalCase.setClosingDate(null);
        }

        LegalCase updated = repository.save(legalCase);

        log.info("Legal case status changed [id={}, from={}, to={}]", id, previous, updated.getStatus());
        return mapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CaseNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Legal case deleted [id={}]", id);
    }
}
