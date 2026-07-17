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
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, InstancioExtension.class})
class LegalCaseServiceTest {

    @Mock
    private LegalCaseRepository repository;

    @Mock
    private LegalCaseMapper mapper;

    @InjectMocks
    private LegalCaseService service;

    private LegalCase sampleCase;
    private LegalCaseResponseDto sampleResponse;
    private LegalCaseRequestDto sampleRequest;

    @BeforeEach
    void setUp() {
        sampleCase = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getId), 1L)
                .set(field(LegalCase::getCaseNumber), "2024-001")
                .set(field(LegalCase::getStatus), CaseStatusEnum.OPEN)
                .set(field(LegalCase::getType), CaseTypeEnum.CIVIL)
                .create();

        sampleResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::caseNumber), "2024-001")
                .set(field(LegalCaseResponseDto::status), CaseStatusEnum.OPEN)
                .set(field(LegalCaseResponseDto::type), CaseTypeEnum.CRIMINAL)
                .create();

        sampleRequest = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::caseNumber), "2024-001")
                .set(field(LegalCaseRequestDto::status), CaseStatusEnum.OPEN)
                .set(field(LegalCaseRequestDto::type), CaseTypeEnum.CRIMINAL)
                .create();
    }

    @Test
    @DisplayName("create: persists case and returns mapped response when case number is unique")
    void create_savesAndReturnsResponse() {
        when(repository.existsByCaseNumber("2024-001")).thenReturn(false);
        when(mapper.toEntity(sampleRequest)).thenReturn(sampleCase);
        when(repository.save(sampleCase)).thenReturn(sampleCase);
        when(mapper.toDto(sampleCase)).thenReturn(sampleResponse);

        LegalCaseResponseDto response = service.create(sampleRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.caseNumber()).isEqualTo("2024-001");
        assertThat(response.status()).isEqualTo(CaseStatusEnum.OPEN);
        verify(mapper).toEntity(sampleRequest);
        verify(repository).save(sampleCase);
        verify(mapper).toDto(sampleCase);
    }

    @Test
    @DisplayName("create: throws DuplicateCaseNumberException and never persists when case number already exists")
    void create_throwsDuplicateCaseNumberException_whenCaseNumberExists() {
        when(repository.existsByCaseNumber("2024-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleRequest))
                .isInstanceOf(DuplicateCaseNumberException.class)
                .hasMessageContaining("2024-001");

        verify(repository, never()).save(any());
        verify(mapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("create: defaults status to OPEN when request does not provide a status")
    void create_setsStatusToOpen_whenRequestStatusIsNull() {
        LegalCaseRequestDto noStatus = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::caseNumber), "2024-002")
                .set(field(LegalCaseRequestDto::status), null)
                .create();

        LegalCase entityWithNullStatus = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getCaseNumber), "2024-002")
                .set(field(LegalCase::getStatus), (CaseStatusEnum) null)
                .create();

        when(repository.existsByCaseNumber("2024-002")).thenReturn(false);
        when(mapper.toEntity(noStatus)).thenReturn(entityWithNullStatus);
        when(repository.save(any())).thenReturn(entityWithNullStatus);
        when(mapper.toDto(any())).thenReturn(sampleResponse);

        service.create(noStatus);

        assertThat(entityWithNullStatus.getStatus()).isEqualTo(CaseStatusEnum.OPEN);
    }

    @Test
    @DisplayName("findById: returns mapped response when the case exists")
    void findById_returnsResponse_whenCaseExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleCase));
        when(mapper.toDto(sampleCase)).thenReturn(sampleResponse);

        LegalCaseResponseDto response = service.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        verify(mapper).toDto(sampleCase);
    }

    @Test
    @DisplayName("findById: throws CaseNotFoundException when no case matches the given id")
    void findById_throwsCaseNotFoundException_whenNotFound() {
        Long randomId = Instancio.create(Long.class);
        when(repository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(randomId))
                .isInstanceOf(CaseNotFoundException.class)
                .hasMessageContaining(String.valueOf(randomId));
    }

    @Test
    @DisplayName("findByCaseNumber: returns mapped response when the case number exists")
    void findByCaseNumber_returnsResponse_whenCaseExists() {
        when(repository.findByCaseNumber("2024-001")).thenReturn(Optional.of(sampleCase));
        when(mapper.toDto(sampleCase)).thenReturn(sampleResponse);

        LegalCaseResponseDto response = service.findByCaseNumber("2024-001");

        assertThat(response.caseNumber()).isEqualTo("2024-001");
    }

    @Test
    @DisplayName("findByCaseNumber: throws CaseNotFoundException when the case number does not exist")
    void findByCaseNumber_throwsCaseNotFoundException_whenNotFound() {
        String randomNumber = Instancio.create(String.class);
        when(repository.findByCaseNumber(randomNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCaseNumber(randomNumber))
                .isInstanceOf(CaseNotFoundException.class)
                .hasMessageContaining(randomNumber);
    }

    @Test
    @DisplayName("findAll: returns all persisted cases as a mapped list")
    void findAll_returnsAllCases() {
        when(repository.findAll()).thenReturn(List.of(sampleCase));
        when(mapper.toDto(sampleCase)).thenReturn(sampleResponse);

        List<LegalCaseResponseDto> responses = service.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByStatus: returns only cases matching the given status")
    void findByStatus_returnsFilteredCases() {
        when(repository.findByStatus(CaseStatusEnum.OPEN)).thenReturn(List.of(sampleCase));
        when(mapper.toDto(sampleCase)).thenReturn(sampleResponse);

        List<LegalCaseResponseDto> responses = service.findByStatus(CaseStatusEnum.OPEN);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(CaseStatusEnum.OPEN);
    }

    @Test
    @DisplayName("findByType: returns only cases matching the given type")
    void findByType_returnsFilteredCases() {
        when(repository.findByType(CaseTypeEnum.CRIMINAL)).thenReturn(List.of(sampleCase));
        when(mapper.toDto(sampleCase)).thenReturn(sampleResponse);

        List<LegalCaseResponseDto> responses = service.findByType(CaseTypeEnum.CRIMINAL);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).type()).isEqualTo(CaseTypeEnum.CRIMINAL);
    }

    @Test
    @DisplayName("update: delegates field update to mapper, saves and returns the updated response")
    void update_delegatesToMapperAndSaves() {
        LegalCaseResponseDto updatedResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::status), CaseStatusEnum.IN_PROGRESS)
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(sampleCase));
        when(repository.save(sampleCase)).thenReturn(sampleCase);
        when(mapper.toDto(sampleCase)).thenReturn(updatedResponse);

        LegalCaseResponseDto response = service.update(1L, sampleRequest);

        assertThat(response.id()).isEqualTo(1L);
        verify(mapper).updateEntity(sampleRequest, sampleCase);
        verify(repository).save(sampleCase);
    }

    @Test
    @DisplayName("update: throws CaseNotFoundException when the target case does not exist")
    void update_throwsCaseNotFoundException_whenNotFound() {
        Long randomId = Instancio.create(Long.class);
        when(repository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(randomId, sampleRequest))
                .isInstanceOf(CaseNotFoundException.class);
    }

    @Test
    @DisplayName("update: throws DuplicateCaseNumberException when the new case number is already used by another record")
    void update_throwsDuplicateCaseNumberException_whenNewNumberInUse() {
        LegalCaseRequestDto requestWithDifferentNumber = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::caseNumber), "2024-999")
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(sampleCase));
        when(repository.existsByCaseNumber("2024-999")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, requestWithDifferentNumber))
                .isInstanceOf(DuplicateCaseNumberException.class)
                .hasMessageContaining("2024-999");
    }

    @Test
    @DisplayName("updateStatus: changes the case status and returns the updated response")
    void updateStatus_changesStatusAndReturnsResponse() {
        LegalCaseResponseDto closedResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::status), CaseStatusEnum.CLOSED)
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(sampleCase));
        when(repository.save(sampleCase)).thenReturn(sampleCase);
        when(mapper.toDto(sampleCase)).thenReturn(closedResponse);

        LegalCaseResponseDto response = service.updateStatus(1L, new UpdateStatusRequestDto(CaseStatusEnum.CLOSED));

        assertThat(response.status()).isEqualTo(CaseStatusEnum.CLOSED);
    }

    @Test
    @DisplayName("updateStatus: sets closingDate to today when status transitions to CLOSED and closingDate was null")
    void updateStatus_setsClosingDateToToday_whenStatusBecomesClosedAndClosingDateIsNull() {
        LegalCase openCase = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getId), 1L)
                .set(field(LegalCase::getStatus), CaseStatusEnum.OPEN)
                .set(field(LegalCase::getClosingDate), null)
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(openCase));
        when(repository.save(openCase)).thenReturn(openCase);
        when(mapper.toDto(openCase)).thenReturn(sampleResponse);

        service.updateStatus(1L, new UpdateStatusRequestDto(CaseStatusEnum.CLOSED));

        assertThat(openCase.getClosingDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("updateStatus: sets closingDate to today when status transitions to ARCHIVED and closingDate was null")
    void updateStatus_setsClosingDateToToday_whenStatusBecomesArchived() {
        LegalCase openCase = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getId), 1L)
                .set(field(LegalCase::getStatus), CaseStatusEnum.CLOSED)
                .set(field(LegalCase::getClosingDate), null)
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(openCase));
        when(repository.save(openCase)).thenReturn(openCase);
        when(mapper.toDto(openCase)).thenReturn(sampleResponse);

        service.updateStatus(1L, new UpdateStatusRequestDto(CaseStatusEnum.ARCHIVED));

        assertThat(openCase.getClosingDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("updateStatus: preserves existing closingDate when status is CLOSED and closingDate was already set")
    void updateStatus_preservesClosingDate_whenAlreadySet() {
        LocalDate existingDate = LocalDate.of(2024, 3, 10);
        LegalCase caseWithDate = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getId), 1L)
                .set(field(LegalCase::getStatus), CaseStatusEnum.IN_PROGRESS)
                .set(field(LegalCase::getClosingDate), existingDate)
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(caseWithDate));
        when(repository.save(caseWithDate)).thenReturn(caseWithDate);
        when(mapper.toDto(caseWithDate)).thenReturn(sampleResponse);

        service.updateStatus(1L, new UpdateStatusRequestDto(CaseStatusEnum.CLOSED));

        assertThat(caseWithDate.getClosingDate()).isEqualTo(existingDate);
    }

    @Test
    @DisplayName("updateStatus: clears closingDate when status transitions back to an active status")
    void updateStatus_clearsClosingDate_whenStatusBecomesActive() {
        LegalCase closedCase = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getId), 1L)
                .set(field(LegalCase::getStatus), CaseStatusEnum.CLOSED)
                .set(field(LegalCase::getClosingDate), LocalDate.of(2024, 6, 1))
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(closedCase));
        when(repository.save(closedCase)).thenReturn(closedCase);
        when(mapper.toDto(closedCase)).thenReturn(sampleResponse);

        service.updateStatus(1L, new UpdateStatusRequestDto(CaseStatusEnum.IN_PROGRESS));

        assertThat(closedCase.getClosingDate()).isNull();
    }

    @Test
    @DisplayName("updateStatus: throws CaseNotFoundException when the target case does not exist")
    void updateStatus_throwsCaseNotFoundException_whenNotFound() {
        Long randomId = Instancio.create(Long.class);
        when(repository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(randomId, new UpdateStatusRequestDto(CaseStatusEnum.CLOSED)))
                .isInstanceOf(CaseNotFoundException.class);
    }

    @Test
    @DisplayName("patch: applies only the provided fields and ignores null ones")
    void patch_appliesProvidedFieldsOnly() {
        PatchLegalCaseRequestDto request = Instancio.of(PatchLegalCaseRequestDto.class)
                .set(field(PatchLegalCaseRequestDto::caseNumber), null)
                .set(field(PatchLegalCaseRequestDto::status), null)
                .set(field(PatchLegalCaseRequestDto::title), "Patched Title")
                .create();

        LegalCaseResponseDto patchedResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::title), "Patched Title")
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(sampleCase));
        when(repository.save(sampleCase)).thenReturn(sampleCase);
        when(mapper.toDto(sampleCase)).thenReturn(patchedResponse);

        LegalCaseResponseDto response = service.patch(1L, request);

        assertThat(response.title()).isEqualTo("Patched Title");
        verify(mapper).patchEntity(request, sampleCase);
        verify(repository).save(sampleCase);
    }

    @Test
    @DisplayName("patch: throws CaseNotFoundException when the target case does not exist")
    void patch_throwsCaseNotFoundException_whenNotFound() {
        Long randomId = Instancio.create(Long.class);
        PatchLegalCaseRequestDto request = Instancio.of(PatchLegalCaseRequestDto.class)
                .set(field(PatchLegalCaseRequestDto::status), null)
                .create();

        when(repository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.patch(randomId, request))
                .isInstanceOf(CaseNotFoundException.class);
    }

    @Test
    @DisplayName("patch: throws DuplicateCaseNumberException when the new case number is already in use")
    void patch_throwsDuplicateCaseNumberException_whenNewNumberInUse() {
        PatchLegalCaseRequestDto request = Instancio.of(PatchLegalCaseRequestDto.class)
                .set(field(PatchLegalCaseRequestDto::caseNumber), "2024-999")
                .set(field(PatchLegalCaseRequestDto::status), null)
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(sampleCase));
        when(repository.existsByCaseNumber("2024-999")).thenReturn(true);

        assertThatThrownBy(() -> service.patch(1L, request))
                .isInstanceOf(DuplicateCaseNumberException.class)
                .hasMessageContaining("2024-999");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("patch: sets closingDate to today when status is patched to CLOSED and closingDate is null")
    void patch_setsClosingDate_whenStatusPatchedToClosed() {
        LegalCase openCase = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getId), 1L)
                .set(field(LegalCase::getStatus), CaseStatusEnum.OPEN)
                .set(field(LegalCase::getClosingDate), null)
                .create();

        PatchLegalCaseRequestDto request = Instancio.of(PatchLegalCaseRequestDto.class)
                .set(field(PatchLegalCaseRequestDto::caseNumber), null)
                .set(field(PatchLegalCaseRequestDto::status), CaseStatusEnum.CLOSED)
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(openCase));
        when(repository.save(openCase)).thenReturn(openCase);
        when(mapper.toDto(openCase)).thenReturn(sampleResponse);

        service.patch(1L, request);

        assertThat(openCase.getClosingDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("patch: clears closingDate when status is patched back to an active status")
    void patch_clearsClosingDate_whenStatusPatchedToActive() {
        LegalCase closedCase = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getId), 1L)
                .set(field(LegalCase::getStatus), CaseStatusEnum.CLOSED)
                .set(field(LegalCase::getClosingDate), LocalDate.of(2024, 6, 1))
                .create();

        PatchLegalCaseRequestDto request = Instancio.of(PatchLegalCaseRequestDto.class)
                .set(field(PatchLegalCaseRequestDto::caseNumber), null)
                .set(field(PatchLegalCaseRequestDto::status), CaseStatusEnum.IN_PROGRESS)
                .create();

        when(repository.findById(1L)).thenReturn(Optional.of(closedCase));
        when(repository.save(closedCase)).thenReturn(closedCase);
        when(mapper.toDto(closedCase)).thenReturn(sampleResponse);

        service.patch(1L, request);

        assertThat(closedCase.getClosingDate()).isNull();
    }

    @Test
    @DisplayName("delete: removes the case from the repository when it exists")
    void delete_deletesCase_whenExists() {        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: throws CaseNotFoundException and never calls deleteById when the case does not exist")
    void delete_throwsCaseNotFoundException_whenNotFound() {
        Long randomId = Instancio.create(Long.class);
        when(repository.existsById(randomId)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(randomId))
                .isInstanceOf(CaseNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
