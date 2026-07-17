package com.evertonhsoethe.apiattus.controller;

import com.evertonhsoethe.apiattus.dto.LegalCaseRequestDto;
import com.evertonhsoethe.apiattus.dto.LegalCaseResponseDto;
import com.evertonhsoethe.apiattus.dto.PatchLegalCaseRequestDto;
import com.evertonhsoethe.apiattus.dto.UpdateStatusRequestDto;
import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import com.evertonhsoethe.apiattus.enums.CaseTypeEnum;
import com.evertonhsoethe.apiattus.exception.CaseNotFoundException;
import com.evertonhsoethe.apiattus.exception.DuplicateCaseNumberException;
import com.evertonhsoethe.apiattus.service.LegalCaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LegalCaseController.class)
@ExtendWith(InstancioExtension.class)
class LegalCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LegalCaseService service;

    private ObjectMapper objectMapper;
    private LegalCaseResponseDto sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        sampleResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::caseNumber), "2024-001")
                .set(field(LegalCaseResponseDto::status), CaseStatusEnum.OPEN)
                .set(field(LegalCaseResponseDto::type), CaseTypeEnum.CRIMINAL)
                .set(field(LegalCaseResponseDto::closingDate), (LocalDate) null)
                .create();
    }

    @Test
    @DisplayName("POST /cases: returns 201 and the created case body when the request is valid")
    void POST_cases_returns201_whenValidRequest() throws Exception {
        LegalCaseRequestDto request = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::caseNumber), "2024-001")
                .set(field(LegalCaseRequestDto::type), CaseTypeEnum.CIVIL)
                .set(field(LegalCaseRequestDto::status), CaseStatusEnum.OPEN)
                .create();

        when(service.create(any(LegalCaseRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.caseNumber").value("2024-001"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("POST /cases: returns 400 with a validation error map when required fields are absent")
    void POST_cases_returns400_whenRequiredFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"only title provided\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").isMap());
    }

    @Test
    @DisplayName("POST /cases: returns 409 when the case number is already registered")
    void POST_cases_returns409_whenCaseNumberAlreadyExists() throws Exception {
        LegalCaseRequestDto request = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::caseNumber), "2024-001")
                .set(field(LegalCaseRequestDto::type), CaseTypeEnum.CIVIL)
                .set(field(LegalCaseRequestDto::status), CaseStatusEnum.OPEN)
                .create();

        when(service.create(any(LegalCaseRequestDto.class)))
                .thenThrow(new DuplicateCaseNumberException("2024-001"));

        mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /cases: returns 200 and a list with all cases when no filter is applied")
    void GET_cases_returns200_withAllCases() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].caseNumber").value("2024-001"));
    }

    @Test
    @DisplayName("GET /cases?status=: returns 200 filtered by status and never calls findAll")
    void GET_cases_filtersByStatus_whenStatusParamProvided() throws Exception {
        when(service.findByStatus(CaseStatusEnum.OPEN)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/cases").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OPEN"));

        verify(service).findByStatus(CaseStatusEnum.OPEN);
        verify(service, never()).findAll();
    }

    @Test
    @DisplayName("GET /cases?type=: returns 200 filtered by type and never calls findAll")
    void GET_cases_filtersByType_whenTypeParamProvided() throws Exception {
        when(service.findByType(CaseTypeEnum.CRIMINAL)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/cases").param("type", "CRIMINAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CRIMINAL"));

        verify(service).findByType(CaseTypeEnum.CRIMINAL);
        verify(service, never()).findAll();
    }

    @Test
    @DisplayName("GET /cases/{id}: returns 200 and the case body when the id exists")
    void GET_cases_id_returns200_whenCaseExists() throws Exception {
        when(service.findById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/cases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.caseNumber").value("2024-001"));
    }

    @Test
    @DisplayName("GET /cases/{id}: returns 404 with error body when the id does not exist")
    void GET_cases_id_returns404_whenCaseDoesNotExist() throws Exception {
        when(service.findById(99L)).thenThrow(new CaseNotFoundException(99L));

        mockMvc.perform(get("/api/v1/cases/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /cases/number/{caseNumber}: returns 200 and the case body when the case number exists")
    void GET_cases_number_returns200_whenCaseExists() throws Exception {
        when(service.findByCaseNumber("2024-001")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/cases/number/2024-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseNumber").value("2024-001"));
    }

    @Test
    @DisplayName("GET /cases/number/{caseNumber}: returns 404 when the case number does not exist")
    void GET_cases_number_returns404_whenCaseDoesNotExist() throws Exception {
        when(service.findByCaseNumber("UNKNOWN")).thenThrow(new CaseNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/api/v1/cases/number/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /cases/{id}: returns 200 with updated case body when the request is valid")
    void PUT_cases_id_returns200_whenValidRequest() throws Exception {
        LegalCaseResponseDto updatedResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::caseNumber), "2024-001")
                .set(field(LegalCaseResponseDto::title), "Updated Title")
                .set(field(LegalCaseResponseDto::status), CaseStatusEnum.IN_PROGRESS)
                .set(field(LegalCaseResponseDto::type), CaseTypeEnum.CIVIL)
                .create();

        LegalCaseRequestDto request = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::caseNumber), "2024-001")
                .set(field(LegalCaseRequestDto::title), "Updated Title")
                .set(field(LegalCaseRequestDto::type), CaseTypeEnum.CIVIL)
                .set(field(LegalCaseRequestDto::status), CaseStatusEnum.IN_PROGRESS)
                .create();

        when(service.update(eq(1L), any(LegalCaseRequestDto.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/cases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PUT /cases/{id}: returns 404 when the case to update does not exist")
    void PUT_cases_id_returns404_whenCaseDoesNotExist() throws Exception {
        LegalCaseRequestDto request = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::caseNumber), "2024-001")
                .set(field(LegalCaseRequestDto::type), CaseTypeEnum.CIVIL)
                .set(field(LegalCaseRequestDto::status), CaseStatusEnum.OPEN)
                .create();

        when(service.update(eq(99L), any(LegalCaseRequestDto.class)))
                .thenThrow(new CaseNotFoundException(99L));

        mockMvc.perform(put("/api/v1/cases/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /cases/{id}: returns 409 when the new case number is already used by another case")
    void PUT_cases_id_returns409_whenCaseNumberConflicts() throws Exception {
        LegalCaseRequestDto request = Instancio.of(LegalCaseRequestDto.class)
                .set(field(LegalCaseRequestDto::caseNumber), "2024-999")
                .set(field(LegalCaseRequestDto::type), CaseTypeEnum.CIVIL)
                .set(field(LegalCaseRequestDto::status), CaseStatusEnum.OPEN)
                .create();

        when(service.update(eq(1L), any(LegalCaseRequestDto.class)))
                .thenThrow(new DuplicateCaseNumberException("2024-999"));

        mockMvc.perform(put("/api/v1/cases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PATCH /cases/{id}: returns 200 with updated fields when only some fields are provided")
    void PATCH_cases_id_returns200_whenPartialFieldsProvided() throws Exception {
        LegalCaseResponseDto patchedResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::caseNumber), "2024-001")
                .set(field(LegalCaseResponseDto::title), "Patched Title")
                .set(field(LegalCaseResponseDto::status), CaseStatusEnum.OPEN)
                .create();

        when(service.patch(eq(1L), any(PatchLegalCaseRequestDto.class))).thenReturn(patchedResponse);

        mockMvc.perform(patch("/api/v1/cases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Patched Title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Patched Title"))
                .andExpect(jsonPath("$.caseNumber").value("2024-001"));
    }

    @Test
    @DisplayName("PATCH /cases/{id}: returns 200 when only status is provided in the body")
    void PATCH_cases_id_returns200_whenOnlyStatusProvided() throws Exception {
        LegalCaseResponseDto closedResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::status), CaseStatusEnum.CLOSED)
                .create();

        when(service.patch(eq(1L), any(PatchLegalCaseRequestDto.class))).thenReturn(closedResponse);

        mockMvc.perform(patch("/api/v1/cases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("PATCH /cases/{id}: returns 404 when the case to patch does not exist")
    void PATCH_cases_id_returns404_whenCaseDoesNotExist() throws Exception {
        when(service.patch(eq(99L), any(PatchLegalCaseRequestDto.class)))
                .thenThrow(new CaseNotFoundException(99L));

        mockMvc.perform(patch("/api/v1/cases/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Any Title\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /cases/{id}: returns 409 when the new case number conflicts with an existing one")
    void PATCH_cases_id_returns409_whenCaseNumberConflicts() throws Exception {
        when(service.patch(eq(1L), any(PatchLegalCaseRequestDto.class)))
                .thenThrow(new DuplicateCaseNumberException("2024-999"));

        mockMvc.perform(patch("/api/v1/cases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNumber\": \"2024-999\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PATCH /cases/{id}: returns 400 when a string field exceeds its maximum length")
    void PATCH_cases_id_returns400_whenFieldExceedsMaxLength() throws Exception {
        String tooLong = "x".repeat(151);

        mockMvc.perform(patch("/api/v1/cases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    @DisplayName("PATCH /cases/{id}/status: returns 200 with the case carrying the new status")
    void PATCH_cases_id_status_returns200_whenValidRequest() throws Exception {
        LegalCaseResponseDto closedResponse = Instancio.of(LegalCaseResponseDto.class)
                .set(field(LegalCaseResponseDto::id), 1L)
                .set(field(LegalCaseResponseDto::status), CaseStatusEnum.CLOSED)
                .create();

        when(service.updateStatus(eq(1L), any(UpdateStatusRequestDto.class))).thenReturn(closedResponse);

        mockMvc.perform(patch("/api/v1/cases/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("PATCH /cases/{id}/status: returns 400 when the status field is absent from the request body")
    void PATCH_cases_id_status_returns400_whenStatusIsNull() throws Exception {
        mockMvc.perform(patch("/api/v1/cases/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /cases/{id}/status: returns 404 when the case to update does not exist")
    void PATCH_cases_id_status_returns404_whenCaseDoesNotExist() throws Exception {
        when(service.updateStatus(eq(99L), any(UpdateStatusRequestDto.class)))
                .thenThrow(new CaseNotFoundException(99L));

        mockMvc.perform(patch("/api/v1/cases/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /cases/{id}: returns 204 with no body when the case exists and is deleted")
    void DELETE_cases_id_returns204_whenCaseExists() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/cases/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    @DisplayName("DELETE /cases/{id}: returns 404 when the case to delete does not exist")
    void DELETE_cases_id_returns404_whenCaseDoesNotExist() throws Exception {
        doThrow(new CaseNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/v1/cases/99"))
                .andExpect(status().isNotFound());
    }
}
