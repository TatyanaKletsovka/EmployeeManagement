package com.syberry.bakery.service.impl;

import com.syberry.bakery.converter.EmployeeConverter;
import com.syberry.bakery.dto.EmployeeDto;
import com.syberry.bakery.dto.EmployeeShortViewDto;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.CreateException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.EmployeeRepository;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static com.syberry.bakery.util.SecurityContextUtil.getUserDetails;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeConverter employeeConverter;
    private final UserRepository userRepository;

    private User getUserByEmailAndBlockedFalse(String email) {
        log.info("Getting user by email and blocked false");
        return userRepository.findByEmailAndIsBlockedFalse(email)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }

    protected Employee getEmployeeByUserEmailAndBlockedFalse(String email) {
        log.info("Getting employee by user email and blocked false");
        return employeeRepository.findByUserEmailIgnoreCaseAndUserIsBlockedFalse(email)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }

    protected Employee getEmployeeByIdAndUserIsBlockedFalse(Long id) {
        log.info("Getting employee by id and user is blocked false");
        return employeeRepository.findByIdAndUserIsBlockedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee is not found"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT')")
    public Page<EmployeeShortViewDto> getAllEmployees(Pageable pageable, String name) {
        return employeeRepository.findByUserIsBlockedFalseAndFilterIn(name, pageable)
                .map(employeeConverter::convertToEmployeeShortViewDto);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT')")
    public EmployeeDto getEmployeeById(Long id) {
        return employeeConverter.convertToEmployeeDto(getEmployeeByIdAndUserIsBlockedFalse(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public EmployeeDto getEmployeeProfile() {
        String email = getUserDetails().getUsername();
        return employeeConverter.convertToEmployeeDto(getEmployeeByUserEmailAndBlockedFalse(email));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        if (employeeDto.getEmail() == null) {
            throw new CreateException("Email is required");
        }
        String email = employeeDto.getEmail();
        User user = getUserByEmailAndBlockedFalse(email);
        if (employeeRepository.findByUserEmailIgnoreCaseAndUserIsBlockedFalse(email).isPresent()) {
            throw new CreateException("Employee is already created");
        }
        Employee employee = employeeConverter.convertToEntity(employeeDto);
        employee.setUser(user);
        return employeeConverter.convertToEmployeeDto(employeeRepository.save(employee));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Transactional
    public EmployeeDto updateEmployeeById(EmployeeDto employeeDto) {
        Employee employeeDb = getEmployeeByIdAndUserIsBlockedFalse(employeeDto.getId());
        Employee employee = employeeConverter.convertToEntity(employeeDto, employeeDb);
        return employeeConverter.convertToEmployeeDto(employee);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Transactional
    public void disableEmployeeById(Long id) {
        Employee employee = getEmployeeByIdAndUserIsBlockedFalse(id);
        employee.setDeletedAt(LocalDateTime.now());
        employee.getUser().setDisabledAt(LocalDateTime.now());
        employee.getUser().setIsBlocked(true);
    }

}
