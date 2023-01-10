package com.syberry.bakery.service;

import com.syberry.bakery.converter.EmployeeConverter;
import com.syberry.bakery.dto.EmployeeDto;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.EmployeeRepository;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.security.UserDetailsImpl;
import com.syberry.bakery.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmployeeServiceTest {

    @InjectMocks
    EmployeeServiceImpl employeeService;
    @Mock
    EmployeeConverter employeeConverter;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @Test
    public void should_SuccessfullyReturnAllEmployee() {
        when(employeeRepository.findByUserIsBlockedFalseAndFilterIn(any(), any())).thenReturn(new PageImpl<>(List.of()));
        employeeService.getAllEmployees(PageRequest.of(0, 20), "");
    }

    @Test
    public void should_SuccessfullyReturnEmployeeById() {
        setContext();
        when(employeeRepository.findByIdAndUserIsBlockedFalse(any())).thenReturn(Optional.of(new Employee()));
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setId(1L);
        when(employeeConverter.convertToEmployeeDto(any(Employee.class))).thenReturn(employeeDto);
        EmployeeDto employee = employeeService.getEmployeeById(1L);
        assertThat(employee.getId()).isEqualTo(1L);
    }

    @Test
    public void should_ThrowError_WhenGettingByIdNoneExistingEmployee() {
        try {
            when(employeeRepository.findByIdAndUserIsBlockedFalse(any())).thenReturn(Optional.empty());
            employeeService.getEmployeeById(1L);
        } catch (EntityNotFoundException ex) {
        }
    }

    @Test
    public void should_SuccessfullyCreateEmployee() {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmail("email@email.com");
        when(userRepository.findByEmailAndIsBlockedFalse(any())).thenReturn(Optional.of(new User()));
        when(employeeConverter.convertToEntity(any(EmployeeDto.class))).thenReturn(new Employee());
        when(employeeRepository.save(any())).thenReturn(new Employee());
        when(employeeConverter.convertToEmployeeDto(any())).thenReturn(new EmployeeDto());
        employeeService.createEmployee(employeeDto);
    }

    @Test
    public void should_SuccessfullyUpdateEmployee() {
        when(employeeRepository.findByIdAndUserIsBlockedFalse(any())).thenReturn(Optional.of(new Employee()));
        when(employeeConverter.convertToEntity(any(), any())).thenReturn(new Employee());
        when(employeeConverter.convertToEmployeeDto(any())).thenReturn(new EmployeeDto());
        employeeService.updateEmployeeById(new EmployeeDto());
    }

    @Test
    public void should_ThrowError_When_UpdatingNoneExistingEmployee() {
        try {
            when(employeeRepository.findByIdAndUserIsBlockedFalse(any())).thenReturn(Optional.empty());
            employeeService.updateEmployeeById(new EmployeeDto());
        } catch (EntityNotFoundException ex) {
        }
    }

    @Test
    public void should_SuccessfullyDisableEmployee() {
        Employee employee = new Employee();
        employee.setUser(new User());
        when(employeeRepository.findByIdAndUserIsBlockedFalse(any())).thenReturn(Optional.of(employee));
        employeeService.disableEmployeeById(any());
    }

    @Test
    public void should_ThrowError_When_DisablingNoneExistingEmployee() {
        try {
            when(employeeRepository.findByIdAndUserIsBlockedFalse(any())).thenReturn(Optional.empty());
            employeeService.disableEmployeeById(any());
        } catch (EntityNotFoundException ex) {
        }
    }

    private void setContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal())
                .thenReturn(new UserDetailsImpl(1L, "test@mail.com", "test@mail.com", Set.of()));
    }
}
