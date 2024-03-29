package com.syberry.bakery.controller;

import com.syberry.bakery.dto.EmployeeDto;
import com.syberry.bakery.dto.EmployeeShortViewDto;
import com.syberry.bakery.service.EmployeeService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/employees")
@Validated
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Page<EmployeeShortViewDto> getAllEmployees (
            Pageable pageable,
            @RequestParam(defaultValue = "") String name) {
        log.info("GET-request: getting all employees");
        return employeeService.getAllEmployees(pageable, name);
    }

    @GetMapping("/{id}")
    public EmployeeDto getEmployeeById(@PathVariable("id") Long id) {
        log.info("GET-request: getting employee with id: {}", id);
        return employeeService.getEmployeeById(id);
    }

    @GetMapping("/authorised")
    public EmployeeDto getEmployeeProfile() {
        log.info("GET-request: getting current employee profile");
        return employeeService.getEmployeeProfile();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        log.info("POST-request: saving employee");
        return employeeService.createEmployee(employeeDto);
    }

    @PutMapping("/{id}")
    public EmployeeDto updateEmployee(@PathVariable("id") Long id,
                                      @Valid @RequestBody EmployeeDto employeeDto) {
        log.info("PUT-request: updating employee with id: {}", id);
        employeeDto.setId(id);
        return employeeService.updateEmployeeById(employeeDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableEmployeeById(@PathVariable("id") Long id) {
        log.info("DELETE-request: deleting employee with id: {}", id);
        employeeService.disableEmployeeById(id);
    }

}
