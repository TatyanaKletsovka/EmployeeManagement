package com.syberry.bakery.service;

import com.syberry.bakery.dto.EmployeeDto;
import com.syberry.bakery.dto.EmployeeShortViewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    Page<EmployeeShortViewDto> getAllEmployees(Pageable pageable, String name);

    EmployeeDto getEmployeeById(Long id);
    EmployeeDto getEmployeeProfile();

    EmployeeDto createEmployee(EmployeeDto employeeDto);

    EmployeeDto updateEmployeeById(EmployeeDto employeeDto);

    void disableEmployeeById(Long id);

}
