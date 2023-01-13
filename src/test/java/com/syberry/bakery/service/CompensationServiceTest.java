package com.syberry.bakery.service;

import com.syberry.bakery.converter.CompensationConverter;
import com.syberry.bakery.dto.CompensationDto;
import com.syberry.bakery.dto.CompensationFilterDto;
import com.syberry.bakery.entity.Compensation;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.exception.CreateException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.CompensationRepository;
import com.syberry.bakery.repository.EmployeeRepository;
import com.syberry.bakery.security.UserDetailsImpl;
import com.syberry.bakery.service.impl.CompensationServiceImpl;
import com.syberry.bakery.service.impl.EmployeeServiceImpl;
import com.syberry.bakery.service.specification.CompensationSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CompensationServiceTest {

    @InjectMocks
    CompensationServiceImpl service;
    @Mock
    EmployeeServiceImpl employeeService;
    @Mock
    CompensationConverter converter;
    @Mock
    CompensationRepository repository;
    @Mock
    @Autowired
    EmployeeRepository employeeRepository;
    @Mock
    CompensationSpecification specification;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @Test
    public void should_SuccessfullyReturnAllCompensations() {
        CompensationFilterDto filter = new CompensationFilterDto("", 0, 1000,
                LocalDate.now(), LocalDate.now(), LocalDate.now(), LocalDate.now(), null, LocalDate.now(), LocalDate.now());
        when(repository.findAll(specification.buildGetAllSpecification(filter), PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of()));
        service.getAllCompensations(filter, PageRequest.of(0, 20));
    }

    @Test
    public void should_SuccessfullyReturnEmployeeCompensations() {
        CompensationFilterDto filter = new CompensationFilterDto("", 0, 1000, LocalDate.now(),
                LocalDate.now(), LocalDate.now(), LocalDate.now(), null, LocalDate.now(), LocalDate.now());
        when(repository.findAll(specification.buildGetAllByEmployeeIdSpecification(1L, filter), PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of()));
        service.getAllCompensations(filter, PageRequest.of(0, 20));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    public void should_SuccessfullyReturnCompensationById() {
        setContext();
        when(repository.findByIdAndEmployeeUserIsBlockedFalse(any()))
                .thenReturn(Optional.of(new Compensation()));
        CompensationDto dto = new CompensationDto();
        dto.setId(1L);
        when(converter.convertToDto(any(Compensation.class))).thenReturn(dto);
        CompensationDto compensationDto = service.getCompensationById(1L);
        assertThat(compensationDto.getId()).isEqualTo(1L);
    }

    @Test
    public void should_ThrowError_WhenGettingByIdNoneExistingCompensation() {
        try {
            when(repository.findByIdAndEmployeeUserIsBlockedFalse(any())).thenReturn(Optional.empty());
            service.getCompensationById(1L);
        } catch (EntityNotFoundException ex) {
        }
    }

    @Test
    public void should_SuccessfullyCreatCompensation() {
        CompensationDto dto = new CompensationDto();
        dto.setEmployeeId(1L);
        dto.setEffectiveFrom(LocalDate.of(2000, 1, 1));
        dto.setValidUntil(LocalDate.of(2000, 1, 2));
        when(employeeRepository.findByIdAndUserIsBlockedFalse(any())).thenReturn(Optional.of(new Employee()));
        when(converter.convertToEntity(any(CompensationDto.class))).thenReturn(new Compensation());
        when(repository.save(any())).thenReturn(new Compensation());
        when(converter.convertToDto(any())).thenReturn(new CompensationDto());
        service.createCompensation(dto);
    }

    @Test
    public void should_ThrowError_When_CreatCompensationWithInvalidDates() {
        try {
            CompensationDto dto = new CompensationDto();
            dto.setEmployeeId(1L);
            dto.setEffectiveFrom(LocalDate.of(2000, 1, 2));
            dto.setValidUntil(LocalDate.of(2000, 1, 1));
            when(employeeRepository.findByIdAndUserIsBlockedFalse(any())).thenReturn(Optional.of(new Employee()));
            when(converter.convertToEntity(any(CompensationDto.class))).thenReturn(new Compensation());
            when(repository.save(any())).thenReturn(new Compensation());
            when(converter.convertToDto(any())).thenReturn(new CompensationDto());
            service.createCompensation(dto);
        } catch (CreateException e) {
        }
    }

    @Test
    public void should_SuccessfullyUpdateCompensation() {
        CompensationDto dto = new CompensationDto();
        dto.setEffectiveFrom(LocalDate.of(2000, 1, 1));
        dto.setValidUntil(LocalDate.of(2000, 1, 2));
        when(repository.findByIdAndEmployeeUserIsBlockedFalse(any())).thenReturn(Optional.of(new Compensation()));
        when(converter.convertToEntity(any(), any())).thenReturn(new Compensation());
        when(converter.convertToDto(any())).thenReturn(new CompensationDto());
        service.updateCompensationById(dto);
    }

    @Test
    public void should_ThrowError_When_UpdatingNoneExistingCompensation() {
        try {
            CompensationDto dto = new CompensationDto();
            dto.setEffectiveFrom(LocalDate.of(2000, 1, 1));
            dto.setValidUntil(LocalDate.of(2000, 1, 2));
            when(repository.findByIdAndEmployeeUserIsBlockedFalse(any())).thenReturn(Optional.empty());
            service.updateCompensationById(dto);
        } catch (EntityNotFoundException ex) {
        }
    }

    @Test
    public void should_SuccessfullyDeleteCompensation() {
        when(repository.findByIdAndEmployeeUserIsBlockedFalse(any())).thenReturn(Optional.of(new Compensation()));
        service.deleteCompensationById(any());
    }

    @Test
    public void should_ThrowError_When_DeletingNoneExistingCompensation() {
        try {
            when(repository.findByIdAndEmployeeUserIsBlockedFalse(any())).thenReturn(Optional.empty());
            service.deleteCompensationById(any());
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
