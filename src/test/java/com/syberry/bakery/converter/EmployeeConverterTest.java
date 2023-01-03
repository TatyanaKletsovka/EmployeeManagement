package com.syberry.bakery.converter;

import com.syberry.bakery.dto.EmployeeDto;
import com.syberry.bakery.dto.Gender;
import com.syberry.bakery.dto.IdType;
import com.syberry.bakery.dto.MaritalStatus;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmployeeConverterTest {
    @InjectMocks
    EmployeeConverter employeeConverter;
    Employee employee = new Employee();
    EmployeeDto employeeDto = new EmployeeDto();

    @BeforeEach
    public void mock_role_user_repositories() {
        employee.setUser(new User());
        employee.getUser().setFirstName("");
        employee.getUser().setLastName("");
        employee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        employee.setPhone("");
        employee.setPersonalEmail("");
        employee.setGender(Gender.MALE);
        employee.setAddressLine1("");
        employee.setAddressLine2("");
        employee.setAddressCountry("");
        employee.setAddressState("");
        employee.setAddressZip("");
        employee.setIdType(IdType.PASSPORT);
        employee.setIdNumber("");
        employee.setCitizenship("");
        employee.setMaritalStatus(MaritalStatus.SINGLE);
        employee.setNumberOfChildren(0);

        employeeDto.setId(1L);
        employeeDto.setFirstName("");
        employeeDto.setLastName("");
        employeeDto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        employeeDto.setPhone("");
        employeeDto.setPersonalEmail("");
        employeeDto.setGender(Gender.MALE);
        employeeDto.setAddressLine1("");
        employeeDto.setAddressLine2("");
        employeeDto.setAddressCountry("");
        employeeDto.setAddressState("");
        employeeDto.setAddressZip("");
        employeeDto.setIdType(IdType.PASSPORT);
        employeeDto.setIdNumber("");
        employeeDto.setCitizenship("");
        employeeDto.setMaritalStatus(MaritalStatus.SINGLE);
        employeeDto.setNumberOfChildren(0);
        employeeDto.setEmail("");


    }

    @Test
    public void should_SuccessfullyConvertToDto() {
        employeeConverter.convertToEmployeeDto(employee);
    }

    @Test
    public void should_SuccessfullyConvertToShortViewDto() {
        employeeConverter.convertToEmployeeShortViewDto(employee);
    }

    @Test
    public void should_SuccessfullyConvertToEntityOneArgument() {
        employeeConverter.convertToEntity(employeeDto);
    }

    @Test
    public void should_SuccessfullyConvertToEntityTwoArgument() {
        employeeConverter.convertToEntity(employeeDto, employee);
    }

}

