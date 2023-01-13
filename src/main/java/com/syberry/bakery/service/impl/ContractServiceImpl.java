package com.syberry.bakery.service.impl;

import com.syberry.bakery.converter.ContractConverter;
import com.syberry.bakery.dto.ContractFullDto;
import com.syberry.bakery.dto.ContractSaveDto;
import com.syberry.bakery.dto.ContractShortDto;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.entity.Contract;
import com.syberry.bakery.entity.Employee;
import com.syberry.bakery.exception.AccessException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.syberry.bakery.util.SecurityContextUtil.getUserDetails;
import static com.syberry.bakery.util.SecurityContextUtil.hasAnyAuthority;
import static com.syberry.bakery.util.SecurityContextUtil.hasAuthority;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT')")
    public Page<ContractShortDto> getAllContracts(Pageable pageable, String name) {
        return contractRepository
                .findByEmployeeIsBlockedFalseAndFilterIn(pageable, name)
                .map(ContractConverter::toShortInfoDto);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public ContractFullDto getByContractId(Long id) {
        Contract contract = contractRepository.findByIdAndEmployeeUserIsBlockedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no such contract"));
        if (hasAnyAuthority(List.of(RoleName.ROLE_ADMIN, RoleName.ROLE_HR, RoleName.ROLE_ACCOUNTANT))
                || (hasAuthority(RoleName.ROLE_USER)
                && Objects.equals(getEmailByContract(contract), getUserDetails().getUsername()))) {
            return ContractConverter.toFullInfoDto(contract);
        }
        throw new AccessException("Viewing other people's contracts is prohibited");

    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT')")
    public List<ContractShortDto> getByEmployeeId(Long id) {
        return contractRepository.findByEmployeeIdAndEmployeeUserIsBlockedFalse(id)
                .stream().map(ContractConverter::toShortInfoDto).toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public List<ContractShortDto> getAllOwnedContracts() {
        return contractRepository.findByEmployeeUserEmailAndEmployeeUserIsBlockedFalse(getUserDetails().getUsername())
                .stream().map(ContractConverter::toShortInfoDto).toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
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
            if (Objects.equals(getUserDetails().getUsername(), getEmailByContract(contract))) {
                throw new CreateException("You can't create a contract for yourself");
            }
            checkContractDates(contract);
            contract.setCreatedAt(LocalDateTime.now());
            return ContractConverter.toFullInfoDto(contractRepository.save(contract));
        } catch (IllegalArgumentException e) {
            throw new CreateException("Contract wasn't saved");
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ContractFullDto updateContract(ContractSaveDto dto) {
        validateProbationPeriod(dto);
        Contract contract = checkFieldsForUpdate(dto);
        if (Objects.equals(getUserDetails().getUsername(), getEmailByContract(contract))) {
            throw new UpdateException("You can't update a contract for yourself");
        }
        checkContractDates(contract);
        contract.setUpdatedAt(LocalDateTime.now());
        return ContractConverter.toFullInfoDto(contractRepository.save(contract));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public void deleteContract(Long id) {
        Optional<Contract> contract = contractRepository.findByIdAndEmployeeUserIsBlockedFalse(id);
        if (contract.isPresent()) {
            if (Objects.equals(getUserDetails().getUsername(), getEmailByContract(contract.get()))) {
                throw new DeleteException("You can't delete your contract");
            }
            contractRepository.deleteById(id);
        } else {
            throw new DeleteException("There is no such contract");
        }
    }

    private void checkContractDates(Contract contract) {
        List<Contract> forCheckData = contractRepository
                .findByEmployeeIdAndContractStartDateOrContractEndDateBetween(contract.getEmployee().getId(),
                contract.getContractStartDate(), contract.getContractEndDate());
        if (forCheckData.size() != 0 && forCheckData.stream().noneMatch((l) -> l.getId().equals(contract.getId()))) {
            throw new CreateException("You already have contract on this period");
        }
        if (contract.getContractStartDate().isAfter(contract.getContractEndDate())){
            throw new CreateException("End data can't be earlier then start data");
        }
        if (contract.getDateOfSignature().isAfter(contract.getContractStartDate())){
            throw new CreateException("Contract start data can't be earlier then date of signature");
        }
    }

    private Contract checkFieldsForUpdate(ContractSaveDto dto) {
        Contract contract = contractRepository.findByIdAndEmployeeUserIsBlockedFalse(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("There is no such contract"));
        if (!Objects.equals(dto.getEmployeeId(), contract.getEmployee().getId())){
            throw new UpdateException("You can't change employee");
        }
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
        if (dto.getProbationPeriod() && dto.getProbationStartDate().isAfter(dto.getProbationEndDate())){
            throw new CreateException("End data can't be earlier then start data");
        }
    }

    private String getEmailByContract(Contract contract) {
        return contract.getEmployee().getUser().getEmail();
    }
}
