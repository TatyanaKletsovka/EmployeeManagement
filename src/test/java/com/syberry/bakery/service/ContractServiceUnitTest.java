package com.syberry.bakery.service;

import com.syberry.bakery.dto.AssignmentType;
import com.syberry.bakery.dto.ContractFullDto;
import com.syberry.bakery.dto.ContractSaveDto;
import com.syberry.bakery.dto.ContractShortDto;
import com.syberry.bakery.dto.Position;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.entity.Contract;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.entity.Role;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.DeleteException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.exception.UpdateException;
import com.syberry.bakery.repository.ContractRepository;
import com.syberry.bakery.repository.EmployeeRepository;
import com.syberry.bakery.security.UserDetailsImpl;
import com.syberry.bakery.service.impl.ContractServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceUnitTest {
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @InjectMocks
    private ContractServiceImpl contractService;
    private Contract contract;
    private ContractSaveDto saveDto;
    private Employee employee;
    private User user;

    @BeforeEach
    void setup() {
        user = new User("firstName", "lastName", "email");
        employee = new Employee();
        contract =
                new Contract(
                        1L,
                        employee,
                        Position.BAKER,
                        LocalDate.now(),
                        LocalDate.now(),
                        LocalDate.now(),
                        AssignmentType.FULL_TIME,
                        false,
                        null,
                        null);
        saveDto =
                new ContractSaveDto(
                        1L,
                        1L,
                        Position.BAKER,
                        LocalDate.now(),
                        LocalDate.now(),
                        LocalDate.now(),
                        AssignmentType.FULL_TIME,
                        false,
                        null,
                        null);
    }

    @Test
    @DisplayName("Should return all contracts when the firstname and lastname are not empty")
    void getAllContractsWhenFirstNameAndLastNameAreNotEmpty() {
        employee.setUser(user);
        Page<Contract> page = new PageImpl<>(List.of(contract));
        when(contractRepository
                .findByEmployeeIsBlockedFalseAndFilterIn(
                        any(), any()))
                .thenReturn(page);
        Page<ContractShortDto> result =
                contractService.getAllContracts(null, "name");
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should throw an exception when the contract does not exist")
    void getByContractIdWhenContractDoesNotExistThenThrowException() {
        when(contractRepository.findByIdAndEmployeeUserIsBlockedFalse(2L))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> contractService.getByContractId(2L));
    }

    @Test
    @DisplayName("Should return contract when the contract exists")
    void getByContractIdWhenContractExistsThenReturnContract() {
        when(contractRepository.findByIdAndEmployeeUserIsBlockedFalse(1L))
                .thenReturn(Optional.of(contract));
        setContext();
        ContractFullDto contractFullDto = contractService.getByContractId(1L);
        assertEquals(contractFullDto.getContractId(), contract.getId());
        assertEquals(contractFullDto.getPosition(), contract.getPosition());
        assertEquals(contractFullDto.getDateOfSignature(), contract.getDateOfSignature());
        assertEquals(contractFullDto.getContractStartDate(), contract.getContractStartDate());
        assertEquals(contractFullDto.getContractEndDate(), contract.getContractEndDate());
        assertEquals(contractFullDto.getType(), contract.getType());
        assertEquals(contractFullDto.getProbationPeriod(), contract.getProbationPeriod());
    }

    @Test
    @DisplayName("Should return all owned leaves")
    void getAllOwnedContractsShouldReturnAllOwnedContracts() {
        employee.setUser(user);
        setContext();
        when(contractRepository.findByEmployeeUserEmailAndEmployeeUserIsBlockedFalse(anyString()))
                .thenReturn(List.of(contract));
        assertEquals(1, contractService.getAllOwnedContracts().size());
    }

    @Test
    @DisplayName("Should return empty list when the employee does not exist")
    void getByEmployeeIdWhenEmployeeDoesNotExistThenReturnEmptyList() {
        when(contractRepository.findByEmployeeIdAndEmployeeUserIsBlockedFalse(1L)).thenReturn(List.of());
        List<ContractShortDto> dtos = contractService.getByEmployeeId(1L);
        assertEquals(0, dtos.size());
    }

    @Test
    @DisplayName("Should return a list of contracts when the employee exists")
    void getByEmployeeIdWhenEmployeeExistsThenReturnListOfContracts() {
        user.setId(1L);
        employee.setUser(user);
        when(contractRepository.findByEmployeeIdAndEmployeeUserIsBlockedFalse(anyLong()))
                .thenReturn(List.of(contract));
        List<ContractShortDto> result = contractService.getByEmployeeId(1L);

        assertEquals(1, result.size());
        verify(contractRepository, times(1)).findByEmployeeIdAndEmployeeUserIsBlockedFalse(anyLong());
    }

    @Test
    @DisplayName("Should throw an exception when the employee is blocked")
    void saveContractWhenEmployeeIsBlockedThenThrowException() {
        when(employeeRepository.findByIdAndUserIsBlockedFalse(anyLong()))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> contractService.saveContract(saveDto));
    }

    @Test
    @DisplayName("Should save the contract when the employee is not blocked")
    void saveContractWhenEmployeeIsNotBlocked() {
        employee.setUser(user);
        when(employeeRepository.findByIdAndUserIsBlockedFalse(anyLong()))
                .thenReturn(Optional.of(employee));
        when(contractRepository.findByEmployeeIdAndEmployeeUserIsBlockedFalse(anyLong()))
                .thenReturn(List.of());
        when(contractRepository.save(any())).thenReturn(contract);
        setContext();
        ContractFullDto contractFullDto = contractService.saveContract(saveDto);

        assertEquals(contractFullDto.getContractId(), contract.getId());
        assertEquals(contractFullDto.getPosition(), contract.getPosition());
        assertEquals(contractFullDto.getDateOfSignature(), contract.getDateOfSignature());
        assertEquals(contractFullDto.getContractStartDate(), contract.getContractStartDate());
        assertEquals(contractFullDto.getContractEndDate(), contract.getContractEndDate());
        assertEquals(contractFullDto.getType(), contract.getType());
        assertEquals(contractFullDto.getProbationPeriod(), contract.getProbationPeriod());
    }

    @Test
    @DisplayName("Should throw an exception when the contract is not found")
    void updateContractWhenContractIsNotFoundThenThrowException() {
        when(contractRepository.findByIdAndEmployeeUserIsBlockedFalse(1L))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> contractService.updateContract(saveDto));
    }

    @Test
    @DisplayName("Should throw an exception when the employee is not found")
    void updateContractWhenEmployeeIsNotFoundThenThrowException() {
        when(contractRepository.findByIdAndEmployeeUserIsBlockedFalse(1L)).thenReturn(Optional.of(contract));
        when(employeeRepository.findByIdAndUserIsBlockedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(UpdateException.class, () -> contractService.updateContract(saveDto));

        verify(contractRepository, times(1)).findByIdAndEmployeeUserIsBlockedFalse(1L);
        verify(employeeRepository, times(1)).findByIdAndUserIsBlockedFalse(1L);
    }

    @Test
    @DisplayName("Should throw an exception when the contract does not exist")
    void deleteContractWhenContractDoesNotExistThenThrowException() {
        when(contractRepository.findByIdAndEmployeeUserIsBlockedFalse(1L))
                .thenReturn(Optional.empty());
        assertThrows(DeleteException.class, () -> contractService.deleteContract(1L));
    }

    @Test
    @DisplayName("Should delete the contract when the contract exists")
    void deleteContractWhenContractExists() {
        employee.setUser(user);
        when(contractRepository.findByIdAndEmployeeUserIsBlockedFalse(1L)).thenReturn(Optional.of(contract));
        setContext();
        contractService.deleteContract(1L);
        verify(contractRepository, times(1)).deleteById(1L);
    }

    private void setContext() {
        Set<Role> roles = Set.of(new Role(1L, RoleName.ROLE_ADMIN));
        List<SimpleGrantedAuthority> list = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .toList();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal())
                .thenReturn(new UserDetailsImpl(1L, "test@mail.com",
                        "test@mail.com", list));
    }
}