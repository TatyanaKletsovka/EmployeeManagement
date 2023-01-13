package com.syberry.bakery.entity;

import com.syberry.bakery.dto.LeaveStatus;
import com.syberry.bakery.dto.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@ToString
@Table(name = "leaves")
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private LeaveType leaveType;
    @Column(name = "leave_start_date")
    private LocalDate leaveStartDate;
    @Column(name = "leave_end_date")
    private LocalDate leaveEndDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_status")
    private LeaveStatus leaveStatus;
    @Column(name = "leave_reason")
    private String leaveReason;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


