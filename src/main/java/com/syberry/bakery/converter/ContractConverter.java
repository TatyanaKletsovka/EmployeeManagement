package com.syberry.bakery.converter;

import com.syberry.bakery.dto.ContractFullDto;
import com.syberry.bakery.dto.ContractSaveDto;
import com.syberry.bakery.dto.ContractShortDto;
import com.syberry.bakery.entity.Contract;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.EmployeeRepository;
import lombok.experimental.UtilityClass;

import java.time.Period;

@UtilityClass
public class ContractConverter {

    public Contract toEntity(ContractSaveDto dto, EmployeeRepository employeeRepository) {
        return new Contract(
                dto.getId(),
                employeeRepository.findByIdAndUserIsBlockedFalse(dto.getEmployeeId())
                        .orElseThrow(() -> new EntityNotFoundException("There is no such employee")),
                dto.getPosition(),
                dto.getDateOfSignature(),
                dto.getContractStartDate(),
                dto.getContractEndDate(),
                dto.getType(),
                dto.getProbationPeriod(),
                dto.getProbationStartDate(),
                dto.getProbationEndDate());
    }

    public ContractShortDto toShortInfoDto(Contract entity) {
        return new ContractShortDto(entity.getEmployee().getId(),
                entity.getEmployee().getUser().getFirstName(),
                entity.getEmployee().getUser().getLastName(),
                entity.getContractStartDate(),
                entity.getContractEndDate());
    }

    public ContractFullDto toFullInfoDto(Contract entity) {
        Period p = Period.between(entity.getContractStartDate(), entity.getContractEndDate());
        int years = p.getYears();
        int months = p.getMonths();
        String fullDate = "" + (years + ((int) (months / 12.0 * 10)) / 10.0);
        return ContractFullDto.builder().contractId(entity.getId())
                .position(entity.getPosition())
                .contractDuration(fullDate)
                .dateOfSignature(entity.getDateOfSignature())
                .contractStartDate(entity.getContractStartDate())
                .contractEndDate(entity.getContractEndDate())
                .type(entity.getType())
                .probationPeriod(entity.getProbationPeriod())
                .probationStartDate(entity.getProbationStartDate())
                .probationEndDate(entity.getProbationEndDate()).build();
    }
}
