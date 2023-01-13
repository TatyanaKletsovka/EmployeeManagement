package com.syberry.bakery.service;

import com.syberry.bakery.dto.LeaveFullDto;
import com.syberry.bakery.dto.LeaveSaveDto;
import com.syberry.bakery.dto.LeaveShortDto;
import com.syberry.bakery.dto.LeaveStatus;
import com.syberry.bakery.dto.LeaveType;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.entity.Leave;
import com.syberry.bakery.entity.Role;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.CreateException;
import com.syberry.bakery.exception.DeleteException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.EmployeeRepository;
import com.syberry.bakery.repository.LeaveRepository;
import com.syberry.bakery.security.UserDetailsImpl;
import com.syberry.bakery.service.impl.LeaveServiceImpl;
import com.syberry.bakery.util.LeaveUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceUnitTest {
    @Mock
    private LeaveRepository leaveRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private LeaveUtil leaveUtil;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private LeaveServiceImpl leaveService;
    private Leave leave;
    private LeaveSaveDto dto;
    private Employee employee;
    private User user;

    @BeforeEach
    void setup() {
        user = new User("firstName", "lastName", "email");
        employee = new Employee();
        leave =
                Leave.builder()
                        .id(1L)
                        .employee(employee)
                        .leaveType(LeaveType.SICK_DAY)
                        .leaveStartDate(LocalDate.of(2023, 1, 20))
                        .leaveEndDate(LocalDate.of(2023, 1, 25))
                        .leaveStatus(LeaveStatus.APPROVED)
                        .build();
        dto =
                new LeaveSaveDto(
                        1L,
                        1L,
                        LeaveType.SICK_DAY,
                        LocalDate.of(2023, 1, 20),
                        LocalDate.of(2023, 1, 22),
                        LeaveStatus.APPROVED,
                        "sick");
    }

    @Test
    @DisplayName(
            "Should return all leaves when name is null, leave type is null and leave status is null")
    void getAllWhenNameIsNullLeaveTypeIsNullAndLeaveStatusIsNull() {
        employee.setUser(user);
        Pageable pageable = PageRequest.of(0, 10);
        List<Leave> leaves = Collections.singletonList(leave);
        Page<Leave> page = new PageImpl<>(leaves);
        when(leaveRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        Page<LeaveShortDto> result = leaveService.getAll(pageable, null, null, null);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName(
            "Should return all leaves when name is not null, leave type is null and leave status is null")
    void getAllWhenNameIsNotNullLeaveTypeIsNullAndLeaveStatusIsNull() {
        employee.setUser(user);
        Pageable pageable = PageRequest.of(0, 10);
        List<Leave> leaves = Collections.singletonList(leave);
        Page<Leave> page = new PageImpl<>(leaves);
        when(leaveRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        Page<LeaveShortDto> result = leaveService.getAll(pageable, "name", null, null);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName(
            "Should return all leaves when name is not null, leave type is not null and leave status is null")
    void getAllWhenNameIsNotNullLeaveTypeIsNotNullAndLeaveStatusIsNull() {
        employee.setUser(user);
        Pageable pageable = PageRequest.of(0, 10);
        List<Leave> leaves = Collections.singletonList(leave);
        Page<Leave> page = new PageImpl<>(leaves);
        when(leaveRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        Page<LeaveShortDto> result = leaveService
                .getAll(pageable, "John", Collections.singletonList(LeaveType.SICK_DAY), null);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName(
            "Should return all leaves when name is not null, leave type is not null and leave status is not null")
    void getAllWhenNameIsNotNullLeaveTypeIsNotNullAndLeaveStatusIsNotNull() {
        employee.setUser(user);
        Pageable pageable = PageRequest.of(0, 10);
        List<Leave> leaves = Collections.singletonList(leave);
        Page<Leave> page = new PageImpl<>(leaves);
        when(leaveRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        Page<LeaveShortDto> result = leaveService.getAll(
                pageable, "John", Collections.singletonList(LeaveType.SICK_DAY),
                Collections.singletonList(LeaveStatus.APPROVED));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should return all leaves of the employee when the employee exists")
    void getByEmployeeIdWhenEmployeeExistsThenReturnAllLeavesOfTheEmployee() {
        employee.setId(1L);
        employee.setUser(user);
        when(leaveRepository.findByEmployeeIdAndEmployeeUserIsBlockedFalse(any()))
                .thenReturn(List.of(leave));
        assertEquals(1, leaveService.getByEmployeeId(1L).size());
    }

    @Test
    @DisplayName("Should return all owned contracts")
    void getAllOwnedContractsShouldReturnAllOwnedContracts() {
        employee.setUser(user);
        setContext();
        when(leaveRepository.findByEmployeeUserEmailAndEmployeeUserIsBlockedFalse(anyString()))
                .thenReturn(List.of(leave));
        assertEquals(1, leaveService.getAllOwnedContracts().size());
    }

    @Test
    @DisplayName("Should throw an exception when the leave does not exist")
    void getByIdWhenLeaveDoesNotExistThenThrowException() {
        when(leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(any()))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> leaveService.getById(1L));
    }

    @Test
    @DisplayName("Should return leave when the leave exists")
    void getByIdWhenLeaveExistsThenReturnLeave() {
        when(leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(any()))
                .thenReturn(Optional.of(leave));
        when(leaveUtil.getRemainingDays(leave, LeaveType.SICK_DAY)).thenReturn(10);
        when(leaveUtil.getRemainingDays(leave, LeaveType.PAID_LEAVE)).thenReturn(10);
        setContext();
        LeaveFullDto result = leaveService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals(LeaveType.SICK_DAY, result.getLeaveType());
        assertEquals(LocalDate.of(2023, 1, 20), result.getLeaveStartDate());
        assertEquals(LocalDate.of(2023, 1, 25), result.getLeaveEndDate());
        assertEquals(6, result.getLeaveDuration());
        assertEquals(LeaveStatus.APPROVED, result.getLeaveStatus());
        assertEquals(10, result.getRemainingSickDays());
        assertEquals(10, result.getRemainingPaidLeaveDays());
    }

    @Test
    @DisplayName(
            "Should throw an exception when the leave type is sick day and the employee has no enough sick days")
    void saveWhenLeaveTypeIsSickDayAndEmployeeHasNoEnoughSickDaysThenThrowException() {
        when(employeeRepository.findByIdAndUserIsBlockedFalse(1L)).thenReturn(Optional.of(employee));
        setContext();
        assertThrows(CreateException.class, () -> leaveService.save(dto));
    }

    @Test
    @DisplayName(
            "Should throw an exception when the leave type is paid leave and the employee has no enough paid leave days")
    void saveWhenLeaveTypeIsPaidLeaveAndEmployeeHasNoEnoughPaidLeaveDaysThenThrowException() {
        dto.setLeaveType(LeaveType.PAID_LEAVE);
        when(employeeRepository.findByIdAndUserIsBlockedFalse(1L)).thenReturn(Optional.of(employee));
        setContext();
        assertThrows(CreateException.class, () -> leaveService.save(dto));
    }

    @Test
    @DisplayName("Should save the leave when the employee has enough sick days")
    void saveWhenEmployeeHasEnoughSickDays() {
        setContext();
        when(leaveRepository.save(any())).thenReturn(leave);
        when(employeeRepository.findByIdAndUserIsBlockedFalse(any()))
                .thenReturn(Optional.of(new Employee()));
        when(leaveUtil.checkRemainingDays(any(Leave.class), any())).thenReturn(true);
        LeaveFullDto result = leaveService.save(dto);
        assertEquals(leave.getId(), result.getId());
        assertEquals(leave.getLeaveType(), result.getLeaveType());
        assertEquals(leave.getLeaveStartDate(), result.getLeaveStartDate());
        assertEquals(leave.getLeaveEndDate(), result.getLeaveEndDate());
        assertEquals((int) DAYS.between(leave.getLeaveStartDate(), leave.getLeaveEndDate()) + 1,
                result.getLeaveDuration());
        assertEquals(leave.getLeaveStatus(), result.getLeaveStatus());
    }

    @Test
    @DisplayName("Should save the leave when the employee has enough paid leave days")
    void saveWhenEmployeeHasEnoughPaidLeaveDays() {
        dto.setLeaveType(LeaveType.PAID_LEAVE);
        setContext();
        when(leaveRepository.save(any())).thenReturn(leave);
        when(employeeRepository.findByIdAndUserIsBlockedFalse(any()))
                .thenReturn(Optional.of(new Employee()));
        when(leaveUtil.checkRemainingDays(any(), any())).thenReturn(true);
        LeaveFullDto saved = leaveService.save(dto);
        assertEquals(leave.getId(), saved.getId());
        assertEquals(leave.getLeaveType(), saved.getLeaveType());
        assertEquals(leave.getLeaveStartDate(), saved.getLeaveStartDate());
        assertEquals(leave.getLeaveEndDate(), saved.getLeaveEndDate());
        assertEquals((int) DAYS.between(leave.getLeaveStartDate(), leave.getLeaveEndDate()) + 1,
                saved.getLeaveDuration());
        assertEquals(leave.getLeaveStatus(), saved.getLeaveStatus());
    }

    @Test
    @DisplayName("Should throw an exception when the leave is not found")
    void updateWhenLeaveIsNotFoundThenThrowException() {
        when(leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(1L))
                .thenReturn(Optional.empty());
        assertThrows(DeleteException.class, () -> leaveService.delete(1L));
    }

    @Test
    @DisplayName("Should throw an exception when the leave type is paid leave and there are not enough paid leave days")
    void updateWhenLeaveTypeIsPaidLeaveAndThereAreNotEnoughPaidLeaveDaysThenThrowException() {
        employee.setUser(user);
        employee.setId(1L);
        dto.setLeaveType(LeaveType.PAID_LEAVE);
        when(leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(1L))
                .thenReturn(Optional.of(leave));
        when(leaveUtil.checkRemainingDays(leave, LeaveType.PAID_LEAVE)).thenReturn(false);
        setContext();
        assertThrows(CreateException.class, () -> leaveService.update(dto));
    }

    @Test
    @DisplayName("Should return a full dto of updated leave when everything is ok")
    void updateWhenEverythingIsOkThenReturnFullDtoOfUpdatedLeave() {
        employee.setUser(user);
        employee.setId(1L);
        when(leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(1L)).thenReturn(Optional.of(leave));
        when(leaveUtil.checkRemainingDays(leave, LeaveType.SICK_DAY)).thenReturn(true);
        setContext();
        LeaveFullDto fullDto = leaveService.update(dto);
        assertEquals(fullDto.getId(), leave.getId());
        assertEquals(fullDto.getLeaveType(), dto.getLeaveType());
        assertEquals(fullDto.getLeaveStartDate(), dto.getLeaveStartDate());
        assertEquals(fullDto.getLeaveEndDate(), dto.getLeaveEndDate());
        assertEquals(DAYS.between(fullDto.getLeaveStartDate(), fullDto.getLeaveEndDate()) + 1,
                fullDto.getLeaveDuration());
        assertEquals(fullDto.getLeaveStatus(), dto.getLeaveStatus());
    }

    @Test
    @DisplayName("Should throw an exception when the leave does not exist")
    void deleteWhenLeaveDoesNotExistThenThrowException() {
        when(leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(1L))
                .thenReturn(Optional.empty());
        assertThrows(DeleteException.class, () -> leaveService.delete(1L));
    }

    @Test
    @DisplayName("Should delete the leave when the leave exists")
    void deleteWhenLeaveExists() {
        employee.setUser(user);
        setContext();
        when(leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(1L)).thenReturn(Optional.of(leave));
        leaveService.delete(1L);
        verify(leaveRepository, times(1)).deleteById(1L);
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