package com.syberry.bakery.converter;

import com.syberry.bakery.dto.AssignmentType;
import com.syberry.bakery.dto.ContractFullDto;
import com.syberry.bakery.dto.ContractSaveDto;
import com.syberry.bakery.dto.ContractShortDto;
import com.syberry.bakery.dto.Position;
import com.syberry.bakery.entity.Contract;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractConverterTest {

    @Mock
    private EmployeeRepository employeeRepository;
    private ContractSaveDto dto;
    private Employee employee;
    private Contract contract;
    private User user;

    @BeforeEach
    void setup() {
        user = new User("firstName", "lastname", "email");
        dto =
                ContractSaveDto.builder()
                        .employeeId(1L)
                        .position(Position.BAKER)
                        .dateOfSignature(LocalDate.now())
                        .contractStartDate(LocalDate.now())
                        .contractEndDate(LocalDate.now().plusYears(1))
                        .type(AssignmentType.FULL_TIME)
                        .probationPeriod(true)
                        .probationStartDate(LocalDate.now())
                        .probationEndDate(LocalDate.now().plusMonths(3))
                        .build();
        employee = Employee.builder().id(1L).user(user).build();
        contract =
                Contract.builder()
                        .id(1L)
                        .employee(employee)
                        .position(Position.BAKER)
                        .dateOfSignature(LocalDate.now())
                        .contractStartDate(LocalDate.now())
                        .contractEndDate(LocalDate.now().plusYears(1))
                        .type(AssignmentType.FULL_TIME)
                        .probationPeriod(true)
                        .probationStartDate(LocalDate.now())
                        .probationEndDate(LocalDate.now().plusMonths(3))
                        .build();
    }

    @Test
    @DisplayName("Should return entity")
    void toEntityWhenEverythingIsOk() {
        when(employeeRepository.findByIdAndUserIsBlockedFalse(1L)).thenReturn(Optional.of(employee));
        Contract contract = ContractConverter.toEntity(dto, employeeRepository);
        assertEquals(dto.getId(), contract.getId());
        assertEquals(dto.getType(), contract.getType());
        assertEquals(dto.getPosition(), contract.getPosition());
        assertEquals(dto.getContractEndDate(), contract.getContractEndDate());
        assertEquals(dto.getContractStartDate(), contract.getContractStartDate());
        assertEquals(dto.getProbationPeriod(), contract.getProbationPeriod());
        assertEquals(dto.getEmployeeId(), contract.getEmployee().getId());
        assertEquals(dto.getDateOfSignature(), contract.getDateOfSignature());
        assertEquals(dto.getProbationStartDate(), contract.getProbationStartDate());
        assertEquals(dto.getProbationEndDate(), contract.getProbationEndDate());
    }

    @Test
    @DisplayName("Should throw an exception when there is no such employee")
    void toEntityWhenThereIsNoSuchEmployeeThenThrowException() {
        when(employeeRepository.findByIdAndUserIsBlockedFalse(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> ContractConverter.toEntity(dto, employeeRepository));
    }

    @Test
    @DisplayName("Should return contractShortDto with correct contract start date")
    void toShortInfoDtoShouldReturnContractShortDtoWithCorrectContractStartDate() {
        ContractShortDto shortDto = ContractConverter.toShortInfoDto(contract);

        assertEquals(contract.getId(), shortDto.getEmployeeId());
        assertEquals(user.getFirstName(), shortDto.getFirstName());
        assertEquals(user.getLastName(), shortDto.getLastName());
        assertEquals(contract.getContractEndDate(), shortDto.getContractEndDate());
        assertEquals(contract.getContractStartDate(), shortDto.getContractStartDate());
    }

    @Test
    @DisplayName("Should return a contractFullDto dto ")
    void toFullInfoDtoWhenEverythingIsOk() {
        ContractFullDto fullDto = ContractConverter.toFullInfoDto(contract);

        assertEquals(contract.getId(), fullDto.getContractId());
        assertEquals(contract.getType(), fullDto.getType());
        assertEquals(contract.getPosition(), fullDto.getPosition());
        assertEquals(contract.getContractEndDate(), fullDto.getContractEndDate());
        assertEquals(contract.getContractStartDate(), fullDto.getContractStartDate());
        assertEquals(contract.getProbationPeriod(), fullDto.getProbationPeriod());
        assertEquals(contract.getDateOfSignature(), fullDto.getDateOfSignature());
        assertEquals(contract.getProbationStartDate(), fullDto.getProbationStartDate());
        assertEquals(contract.getProbationEndDate(), fullDto.getProbationEndDate());
    }
}