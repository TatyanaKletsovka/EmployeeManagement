package com.syberry.bakery.converter;

import com.syberry.bakery.dto.EmployeeDto;
import com.syberry.bakery.dto.EmployeeShortViewDto;
import com.syberry.bakery.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EmployeeConverter {

    public Employee convertToEntity(EmployeeDto dto) {
        Employee employee = buildEmployeeStandardFields(dto, new Employee());
        employee.setCreatedAt(LocalDateTime.now());
        return employee;
    }

    public Employee convertToEntity(EmployeeDto dto, Employee emp) {
        Employee employee = buildEmployeeStandardFields(dto, emp);
        employee.setUpdatedAt(LocalDateTime.now());
        employee.getUser().setUpdatedAt(LocalDateTime.now());
        if (dto.getFirstName() != null) {
            employee.getUser().setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            employee.getUser().setLastName(dto.getLastName());
        }
        return employee;
    }

    private Employee buildEmployeeStandardFields(EmployeeDto dto, Employee employee) {
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setPhone(dto.getPhone());
        employee.setPersonalEmail(dto.getPersonalEmail());
        employee.setGender(dto.getGender());
        employee.setAddressLine1(dto.getAddressLine1());
        employee.setAddressLine2(dto.getAddressLine2());
        employee.setAddressCountry(dto.getAddressCountry());
        employee.setAddressState(dto.getAddressState());
        employee.setAddressZip(dto.getAddressZip());
        employee.setIdType(dto.getIdType());
        employee.setIdNumber(dto.getIdNumber());
        employee.setCitizenship(dto.getCitizenship());
        employee.setMaritalStatus(dto.getMaritalStatus());
        employee.setNumberOfChildren(dto.getNumberOfChildren());
        return employee;
    }

    public EmployeeShortViewDto convertToEmployeeShortViewDto(Employee employee) {
        return EmployeeShortViewDto.builder()
                .id(employee.getId())
                .firstName(employee.getUser().getFirstName())
                .lastName(employee.getUser().getLastName())
                .build();
    }

    public EmployeeDto convertToEmployeeDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getUser().getFirstName())
                .lastName(employee.getUser().getLastName())
                .dateOfBirth(employee.getDateOfBirth())
                .phone(employee.getPhone())
                .personalEmail(employee.getPersonalEmail())
                .gender(employee.getGender())
                .addressLine1(employee.getAddressLine1())
                .addressLine2(employee.getAddressLine2())
                .addressCountry(employee.getAddressCountry())
                .addressState(employee.getAddressState())
                .addressZip(employee.getAddressZip())
                .idType(employee.getIdType())
                .idNumber(employee.getIdNumber())
                .citizenship(employee.getCitizenship())
                .maritalStatus(employee.getMaritalStatus())
                .numberOfChildren(employee.getNumberOfChildren())
                .email(employee.getUser().getEmail())
                .build();
    }

}
