package com.syberry.bakery.entity;

import com.syberry.bakery.dto.AssignmentType;
import com.syberry.bakery.dto.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;
    @Enumerated(EnumType.STRING)
    @Column(name = "position")
    private Position position;
    @Column(name = "date_of_Signature")
    private LocalDate dateOfSignature;
    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;
    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type")
    private AssignmentType type;
    @Column(name = "probation_period")
    private Boolean probationPeriod = false;
    @Column(name = "probation_start_date")
    private LocalDate probationStartDate;
    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Contract(Long id, Employee employee, Position position, LocalDate dateOfSignature,
                    LocalDate contractStartDate, LocalDate contractEndDate, AssignmentType type,
                    Boolean probationPeriod, LocalDate probationStartDate, LocalDate probationEndDate) {
        this.id = id;
        this.employee = employee;
        this.position = position;
        this.dateOfSignature = dateOfSignature;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.type = type;
        this.probationPeriod = probationPeriod;
        this.probationStartDate = probationStartDate;
        this.probationEndDate = probationEndDate;
    }
}
