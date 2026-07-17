package com.evertonhsoethe.apiattus.mapper;

import com.evertonhsoethe.apiattus.domain.LegalCase;
import com.evertonhsoethe.apiattus.dto.LegalCaseRequestDto;
import com.evertonhsoethe.apiattus.dto.LegalCaseResponseDto;
import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@ExtendWith(InstancioExtension.class)
class LegalCaseMapperTest {

    private final LegalCaseMapper mapper = Mappers.getMapper(LegalCaseMapper.class);

    @Test
    @DisplayName("toDto: maps every entity field to the corresponding DTO field")
    void toDto_mapsAllFields() {
        LegalCase entity = Instancio.create(LegalCase.class);

        LegalCaseResponseDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.caseNumber()).isEqualTo(entity.getCaseNumber());
        assertThat(dto.title()).isEqualTo(entity.getTitle());
        assertThat(dto.description()).isEqualTo(entity.getDescription());
        assertThat(dto.status()).isEqualTo(entity.getStatus());
        assertThat(dto.type()).isEqualTo(entity.getType());
        assertThat(dto.plaintiff()).isEqualTo(entity.getPlaintiff());
        assertThat(dto.defendant()).isEqualTo(entity.getDefendant());
        assertThat(dto.filingDate()).isEqualTo(entity.getFilingDate());
        assertThat(dto.closingDate()).isEqualTo(entity.getClosingDate());
        assertThat(dto.createdAt()).isEqualTo(entity.getCreatedAt());
        assertThat(dto.updatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("toEntity: maps request DTO fields to entity and leaves id, createdAt and updatedAt null")
    void toEntity_mapsAllFields_andIgnoresAuditFields() {
        LegalCaseRequestDto dto = Instancio.create(LegalCaseRequestDto.class);

        LegalCase entity = mapper.toEntity(dto);

        assertThat(entity.getCaseNumber()).isEqualTo(dto.caseNumber());
        assertThat(entity.getTitle()).isEqualTo(dto.title());
        assertThat(entity.getDescription()).isEqualTo(dto.description());
        assertThat(entity.getType()).isEqualTo(dto.type());
        assertThat(entity.getStatus()).isEqualTo(dto.status());
        assertThat(entity.getPlaintiff()).isEqualTo(dto.plaintiff());
        assertThat(entity.getDefendant()).isEqualTo(dto.defendant());
        assertThat(entity.getFilingDate()).isEqualTo(dto.filingDate());
        assertThat(entity.getClosingDate()).isEqualTo(dto.closingDate());

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("toEntity: produces an entity with null status when the DTO status is null")
    void toEntity_withNullStatus_leavesStatusNull() {
        LegalCaseRequestDto dto = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::status), null)
                .create();

        LegalCase entity = mapper.toEntity(dto);

        assertThat(entity.getStatus()).isNull();
    }

    @Test
    @DisplayName("updateEntity: overwrites all mapped fields on the existing entity while preserving id")
    void updateEntity_updatesAllMappedFields_andPreservesId() {
        LegalCase existing = Instancio.create(LegalCase.class);
        LegalCaseRequestDto dto = Instancio.create(LegalCaseRequestDto.class);

        Long originalId = existing.getId();

        mapper.updateEntity(dto, existing);

        assertThat(existing.getCaseNumber()).isEqualTo(dto.caseNumber());
        assertThat(existing.getTitle()).isEqualTo(dto.title());
        assertThat(existing.getDescription()).isEqualTo(dto.description());
        assertThat(existing.getType()).isEqualTo(dto.type());
        assertThat(existing.getPlaintiff()).isEqualTo(dto.plaintiff());
        assertThat(existing.getDefendant()).isEqualTo(dto.defendant());
        assertThat(existing.getFilingDate()).isEqualTo(dto.filingDate());
        assertThat(existing.getClosingDate()).isEqualTo(dto.closingDate());
        assertThat(existing.getId()).isEqualTo(originalId);
    }

    @Test
    @DisplayName("toDto: handles null optional fields (description, closingDate) without throwing")
    void toDto_withNullOptionalFields_doesNotThrow() {
        LegalCase entity = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getDescription), null)
                .set(field(LegalCase::getClosingDate), null)
                .create();

        LegalCaseResponseDto dto = mapper.toDto(entity);

        assertThat(dto.description()).isNull();
        assertThat(dto.closingDate()).isNull();
    }

    @Test
    @DisplayName("toDto: mapping is consistent across 10 independently randomized entity instances")
    void toDto_isConsistentAcrossMultipleRandomInstances() {
        for (int i = 0; i < 10; i++) {
            LegalCase entity = Instancio.create(LegalCase.class);
            LegalCaseResponseDto dto = mapper.toDto(entity);

            assertThat(dto.id()).isEqualTo(entity.getId());
            assertThat(dto.caseNumber()).isEqualTo(entity.getCaseNumber());
            assertThat(dto.status()).isEqualTo(entity.getStatus());
        }
    }

    @Test
    @DisplayName("updateEntity: sets status to null on the entity when the DTO status is null")
    void updateEntity_withNullStatus_setsStatusToNull() {
        LegalCase existing = Instancio.of(LegalCase.class)
                .set(field(LegalCase::getStatus), CaseStatusEnum.IN_PROGRESS)
                .create();

        LegalCaseRequestDto dto = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::status), null)
                .create();

        mapper.updateEntity(dto, existing);

        assertThat(existing.getStatus()).isNull();
    }
}
