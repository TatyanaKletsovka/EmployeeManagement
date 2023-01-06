package com.syberry.bakery.service.impl;

import com.syberry.bakery.converter.ContractConverter;
import com.syberry.bakery.dto.ContractFullDto;
import com.syberry.bakery.dto.ContractSaveDto;
import com.syberry.bakery.dto.ContractShortDto;
import com.syberry.bakery.entity.Contract;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.exception.CreateException;
import com.syberry.bakery.exception.DeleteException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.exception.UpdateException;
import com.syberry.bakery.repository.ContractRepository;
import com.syberry.bakery.repository.EmployeeRepository;
import com.syberry.bakery.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public Page<ContractShortDto> getAllContracts(Pageable pageable, String name) {
        return contractRepository
                .findByEmployeeIsBlockedFalseAndFilterIn(pageable, name)
                .map(ContractConverter::toShortInfoDto);
    }

    @Override
    public ContractFullDto getByContractId(Long id) {
        return ContractConverter.toFullInfoDto(contractRepository.findByIdAndEmployeeUserIsBlockedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no such contract")));
    }

    @Override
    public List<ContractShortDto> getByEmployeeId(Long id) {
        return contractRepository.findByEmployeeIdAndEmployeeUserIsBlockedFalse(id)
                .stream().map(ContractConverter::toShortInfoDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractFullDto saveContract(ContractSaveDto dto) {
        try {
            if (contractRepository.findByEmployeeIdAndEmployeeUserIsBlockedFalse(dto.getEmployeeId()).isEmpty()) {
                Employee employee = employeeRepository.findByIdAndUserIsBlockedFalse(dto.getEmployeeId())
                        .orElseThrow(() -> new EntityNotFoundException("There is no such user"));
                employee.setDateOfJoin(dto.getContractStartDate());
                employeeRepository.save(employee);
            }
            validateProbationPeriod(dto);
            Contract contract = ContractConverter.toEntity(dto, employeeRepository);
            contract.setCreatedAt(LocalDateTime.now());
            return ContractConverter.toFullInfoDto(contractRepository.save(contract));
        } catch (IllegalArgumentException e) {
            throw new CreateException("Contract wasn't saved");
        }
    }

    @Override
    @Transactional
    public ContractFullDto updateContract(ContractSaveDto dto) {
        validateProbationPeriod(dto);
        Contract contract = checkFieldsForUpdate(dto);
        contract.setUpdatedAt(LocalDateTime.now());
        return ContractConverter.toFullInfoDto(contractRepository.save(contract));
    }

    private Contract checkFieldsForUpdate(ContractSaveDto dto) {
        Contract contract = contractRepository.findByIdAndEmployeeUserIsBlockedFalse(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("There is no such contract"));
        contract.setEmployee(employeeRepository.findByIdAndUserIsBlockedFalse(dto.getEmployeeId())
                .orElseThrow(() -> new UpdateException("There is no such employee")));
        contract.setPosition(dto.getPosition());
        contract.setDateOfSignature(dto.getDateOfSignature());
        contract.setContractStartDate(dto.getContractStartDate());
        contract.setContractEndDate(dto.getContractEndDate());
        contract.setType(dto.getType());
        contract.setProbationPeriod(dto.getProbationPeriod());
        contract.setProbationStartDate(dto.getProbationStartDate());
        contract.setProbationEndDate(dto.getProbationEndDate());
        return contract;
    }
    private static void validateProbationPeriod(ContractSaveDto dto) {
        if (dto.getProbationPeriod() && (dto.getProbationStartDate() == null || dto.getProbationEndDate() == null)) {
            throw new CreateException("If the employee has a probation period, then the dates must be filled in");
        } else if (!dto.getProbationPeriod() && (dto.getProbationStartDate() != null || dto.getProbationEndDate() != null)) {
            throw new CreateException("If the employee doesn't have a probation period, the dates must be empty");
        }
    }

    @Override
    public void deleteContract(Long id) {
        if (contractRepository.findByIdAndEmployeeUserIsBlockedFalse(id).isPresent()) {
            contractRepository.deleteById(id);
        } else {
            throw new DeleteException("There is no such contract");
        }
    }
}
