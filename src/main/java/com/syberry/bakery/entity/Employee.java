package com.syberry.bakery.entity;

import com.syberry.bakery.dto.Gender;
import com.syberry.bakery.dto.IdType;
import com.syberry.bakery.dto.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NotNull
    @Column(name = "date_of_birth")
    @Past(message = "Date of birth can't be in future")
    private LocalDate dateOfBirth;

    @NotNull
    @Column(name = "phone")
    @Size(min = 5, max = 10)
    @Pattern(regexp = "^\\d{10}$", message = "The phone number must consist of 10 digits")
    private String phone;

    @NotNull
    @Column(name = "personal_email", length = 255)
    @Email
    private String personalEmail;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "gender_id")
    private Gender gender = Gender.MALE;

    @NotNull
    @Column(name = "address_line_1", length = 100)
    private String addressLine1;

    @Column(name = "address_line_2", length = 100)
    private String addressLine2;

    @NotNull
    @Column(name = "address_country", length = 255)
    private String addressCountry;

    @NotNull
    @Column(name = "address_state", length = 2)
    private String addressState;

    @Column(name = "address_zip", length = 10)
    private String addressZip;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "id_type_id")
    private IdType idType = IdType.PASSPORT;

    @NotNull
    @Column(name = "id_number", length = 10)
    private String idNumber;

    @NotNull
    @Column(name = "citizenship", length = 255)
    private String citizenship;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "marital_status_id")
    private MaritalStatus maritalStatus = MaritalStatus.SINGLE;

    @NotNull
    @Column(name = "number_of_children")
    private int numberOfChildren = 0;

    @Column(name = "date_of_join")
    private LocalDate dateOfJoin;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}
