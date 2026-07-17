package com.evertonhsoethe.apiattus.domain;

import com.evertonhsoethe.apiattus.enums.CaseStatusEnum;
import com.evertonhsoethe.apiattus.enums.CaseTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "legal_case")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LegalCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", nullable = false, unique = true, length = 50)
    private String caseNumber;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseTypeEnum type;

    @Column(name = "plaintiff", nullable = false, length = 150)
    private String plaintiff;

    @Column(name = "defendant", nullable = false, length = 150)
    private String defendant;

    @Column(name = "filing_date", nullable = false)
    private LocalDate filingDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = CaseStatusEnum.OPEN;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
