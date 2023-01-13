package com.syberry.bakery.service;

import com.syberry.bakery.dto.CompensationDto;
import com.syberry.bakery.dto.CompensationFilterDto;
import com.syberry.bakery.dto.CompensationShortViewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompensationService {

    Page<CompensationShortViewDto> getAllCompensations(CompensationFilterDto filterDto, Pageable pageable);

    Page<CompensationShortViewDto> getCompensationsByEmployeeId(Long id, CompensationFilterDto filterDto,
                                                                Pageable pageable);
    Page<CompensationShortViewDto> getAuthorisedUserCompensations(CompensationFilterDto filterDto, Pageable pageable);

    CompensationDto getCompensationById(Long id);

    CompensationDto getAuthorisedUserCompensationById(Long id);

    CompensationDto createCompensation(CompensationDto compensationDto);

    CompensationDto updateCompensationById(CompensationDto createCompensationDto);

    void deleteCompensationById(Long id);
}
