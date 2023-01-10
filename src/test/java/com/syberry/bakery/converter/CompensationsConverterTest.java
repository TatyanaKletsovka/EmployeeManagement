package com.syberry.bakery.converter;

import com.syberry.bakery.dto.CompensationDto;
import com.syberry.bakery.entity.Compensation;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.repository.CompensationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CompensationsConverterTest {
    @InjectMocks
    CompensationConverter converter;
    @Mock
    CompensationRepository repository;
    Compensation compensation = new Compensation();
    CompensationDto compensationDto = new CompensationDto();

    @BeforeEach
    public void setup() {
        User user = new User();
        user.setFirstName("FirstName");
        user.setLastName("LastName");

        Employee employee = new Employee();
        employee.setUser(user);

        compensation.setId(1L);
        compensation.setAmount(500);
        compensation.setEffectiveFrom(LocalDate.of(2000, 1, 1));
        compensation.setValidUntil(LocalDate.of(2000, 1, 2));
        compensation.setSpecialConditions("Special conditions");
        compensation.setEmployee(employee);

        compensationDto.setId(1L);
        compensationDto.setAmount(500);
        compensationDto.setEffectiveFrom(LocalDate.of(2000, 1, 1));
        compensationDto.setValidUntil(LocalDate.of(2000, 1, 2));
        compensationDto.setSpecialConditions("Special conditions");
        compensationDto.setEmployeeId(1L);
    }


    @Test
    public void should_SuccessfullyConvertToEntityOneArgument() {
        converter.convertToEntity(compensationDto);
    }

    @Test
    public void should_SuccessfullyConvertToEntityTwoArgument() {
        converter.convertToEntity(compensationDto, compensation);
    }

    @Test
    public void should_SuccessfullyConvertToDto() {
        converter.convertToDto(compensation);
    }

    @Test
    public void should_SuccessfullyConvertToShortViewDto() {
        converter.convertToShortViewDto(compensation);
    }

}

