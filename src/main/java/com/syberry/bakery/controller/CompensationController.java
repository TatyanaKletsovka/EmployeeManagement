package com.syberry.bakery.controller;

import com.syberry.bakery.dto.CompensationDto;
import com.syberry.bakery.dto.CompensationFilterDto;
import com.syberry.bakery.dto.CompensationShortViewDto;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.service.CompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.syberry.bakery.util.SecurityContextUtil.hasAuthority;

@RestController
@RequestMapping(path = "/compensations")
@Validated
@Slf4j
@RequiredArgsConstructor
public class CompensationController {

    private final CompensationService compensationService;

    @GetMapping
    public Page<CompensationShortViewDto> getAllCompensations (
            CompensationFilterDto filterDto, Pageable pageable) {
        log.info("GET-request: getting all compensations");
        return compensationService.getAllCompensations(filterDto, pageable);
    }

    @GetMapping("/authorised")
    public Page<CompensationShortViewDto> getAuthorisedUserCompensations (
            CompensationFilterDto filterDto, Pageable pageable) {
        log.info("GET-request: getting authorised user compensations");
        return compensationService.getAuthorisedUserCompensations(filterDto, pageable);
    }

    @GetMapping("/employees/{id}")
    public Page<CompensationShortViewDto> getCompensationsByEmployeeId (
            @PathVariable("id") Long id,
            CompensationFilterDto filterDto,
            Pageable pageable) {
        log.info("GET-request: getting compensations for employee with id: {}", id);
        return compensationService.getCompensationsByEmployeeId(id, filterDto, pageable);
    }

    @GetMapping("/{id}")
    public CompensationDto getCompensationById(@PathVariable("id") Long id) {
        log.info("GET-request: getting compensation with id: {}", id);
        if (hasAuthority(RoleName.ROLE_ADMIN) || hasAuthority(RoleName.ROLE_ACCOUNTANT)) {
            return compensationService.getCompensationById(id);
        } else {
            return compensationService.getAuthorisedUserCompensationById(id);
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompensationDto createCompensation(@Valid @RequestBody CompensationDto compensationDto) {
        log.info("POST-request: saving compensation");
        return compensationService.createCompensation(compensationDto);
    }

    @PutMapping("/{id}")
    public CompensationDto updateCompensation(@PathVariable("id") Long id,
                                      @Valid @RequestBody CompensationDto compensationDto) {
        log.info("PUT-request: updating compensation with id: {}", id);
        compensationDto.setId(id);
        return compensationService.updateCompensationById(compensationDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompensationById(@PathVariable("id") Long id) {
        log.info("DELETE-request: deleting compensation with id: {}", id);
        compensationService.deleteCompensationById(id);
    }

}
