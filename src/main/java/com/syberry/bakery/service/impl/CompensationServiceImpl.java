package com.syberry.bakery.service.impl;

import com.syberry.bakery.converter.CompensationConverter;
import com.syberry.bakery.dto.CompensationDto;
import com.syberry.bakery.dto.CompensationFilterDto;
import com.syberry.bakery.dto.CompensationShortViewDto;
import com.syberry.bakery.entity.Compensation;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.exception.AccessException;
import com.syberry.bakery.exception.CreateException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.CompensationRepository;
import com.syberry.bakery.service.CompensationService;
import com.syberry.bakery.service.specification.CompensationSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

import static com.syberry.bakery.util.SecurityContextUtil.getUserDetails;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompensationServiceImpl implements CompensationService {

    private final CompensationRepository compensationRepository;
    private final EmployeeServiceImpl employeeService;
    private final CompensationConverter compensationConverter;
    private final CompensationSpecification specification;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public Page<CompensationShortViewDto> getAllCompensations(CompensationFilterDto filter, Pageable pageable) {
        return compensationRepository.findAll(specification.buildGetAllSpecification(filter), pageable)
                .map(compensationConverter::convertToShortViewDto);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public Page<CompensationShortViewDto> getCompensationsByEmployeeId(Long id, CompensationFilterDto filter,
                                                                       Pageable pageable) {
        return compensationRepository.findAll(specification.buildGetAllByEmployeeIdSpecification(id, filter), pageable)
                .map(compensationConverter::convertToShortViewDto);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public Page<CompensationShortViewDto> getAuthorisedUserCompensations(CompensationFilterDto filter, Pageable pageable) {
        String email = getUserDetails().getUsername();
        Long employeeId = employeeService.getEmployeeByUserEmailAndBlockedFalse(email).getId();
        return compensationRepository.findAll(specification.buildGetAllByEmployeeIdSpecification(employeeId, filter), pageable)
                .map(compensationConverter::convertToShortViewDto);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public CompensationDto getCompensationById(Long id) {
        return compensationConverter.convertToDto(getCompensationByIdAndUserIsBlockedFalse(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public CompensationDto getAuthorisedUserCompensationById(Long id) {
        Compensation compensation = getCompensationByIdAndUserIsBlockedFalse(id);
        if (!Objects.equals(compensation.getEmployee().getUser().getEmail(),
                getUserDetails().getUsername())) {
            throw new AccessException("The user can only view his own information");
        }
        return compensationConverter.convertToDto(compensation);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public CompensationDto createCompensation(CompensationDto dto) {
        Employee employee = employeeService.getEmployeeByIdAndUserIsBlockedFalse(dto.getEmployeeId());
        validateDates(dto);
        Compensation compensation = compensationConverter.convertToEntity(dto);
        compensation.setEmployee(employee);
        return compensationConverter.convertToDto(compensationRepository.save(compensation));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Transactional
    public CompensationDto updateCompensationById(CompensationDto dto) {
        validateDates(dto);
        Compensation compensation = getCompensationByIdAndUserIsBlockedFalse(dto.getId());
        compensation = compensationConverter.convertToEntity(dto, compensation);
        return compensationConverter.convertToDto(compensation);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Transactional
    public void deleteCompensationById(Long id) {
        getCompensationByIdAndUserIsBlockedFalse(id);
        compensationRepository.deleteById(id);
    }

    private Compensation getCompensationByIdAndUserIsBlockedFalse(Long id) {
        log.info("Getting compensation by id and user isBlocked false");
        return compensationRepository.findByIdAndEmployeeUserIsBlockedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Compensation is not found"));
    }

    private void validateDates(CompensationDto dto) {
        if (dto.getValidUntil().isBefore(dto.getEffectiveFrom()) || dto.getValidUntil().equals(dto.getEffectiveFrom())) {
            throw new CreateException("validUntil date must be later than effectiveFrom date");
        }
        List<Compensation> list = compensationRepository.findByDatesCrossingInEmployee(dto.getEmployeeId(),
                dto.getEffectiveFrom(), dto.getValidUntil());
        if (list.size() > 1 || (list.size() == 1 && !Objects.equals(list.get(0).getId(), dto.getId()))) {
            throw new CreateException("effectiveFrom and validUntil dates cannot overlap with other employee's compensations");
        }
    }
}
