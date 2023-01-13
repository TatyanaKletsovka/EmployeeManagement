package com.syberry.bakery.controller;

import com.syberry.bakery.dto.ContractFullDto;
import com.syberry.bakery.dto.ContractSaveDto;
import com.syberry.bakery.dto.ContractShortDto;
import com.syberry.bakery.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import java.util.List;

import static com.syberry.bakery.util.SecurityContextUtil.getUserDetails;

@RestController
@RequestMapping("/contracts")
@Slf4j
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @GetMapping
    public Page<ContractShortDto> getAllContracts(Pageable pageable,
                                                  @RequestParam(defaultValue = "") String name) {
        log.info("GET-request: getting all contracts");
        return contractService.getAllContracts(pageable, name);
    }

    @GetMapping("/{id}")
    public ContractFullDto getContractById(@PathVariable("id") Long id) {
        log.info("GET-request: getting contract with id: {}", id);
        return contractService.getByContractId(id);
    }

    @GetMapping("/employees/{id}")
    public List<ContractShortDto> getUserContracts(@PathVariable("id") Long id) {
        log.info("GET-request: getting contracts for user with id: {}", id);
        return contractService.getByEmployeeId(id);
    }

    @GetMapping("/employees")
    public List<ContractShortDto> getOwnedContracts() {
        log.info("GET-request: getting contracts for user(themself) with username: {}", getUserDetails().getUsername());
        return contractService.getAllOwnedContracts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractFullDto addContract(@Valid @RequestBody ContractSaveDto dto) {
        log.info("POST-request: saving contract");
        return contractService.saveContract(dto);
    }

    @PutMapping("/{id}")
    public ContractFullDto updateContract(@PathVariable("id") Long id, @Valid @RequestBody ContractSaveDto dto) {
        log.info("PUT-request: updating contract with id: {}", id);
        dto.setId(id);
        return contractService.updateContract(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContract(@PathVariable("id") Long id) {
        log.info("DELETE-request: deleting contract with id: {}", id);
        contractService.deleteContract(id);
    }
}

