package com.syberry.bakery.converter;

import com.syberry.bakery.dto.CompensationDto;
import com.syberry.bakery.dto.CompensationShortViewDto;
import com.syberry.bakery.entity.Compensation;
import com.syberry.bakery.repository.CompensationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CompensationConverter {

    private final CompensationRepository compensationRepository;

    public Compensation convertToEntity(CompensationDto dto) {
        return buildCompensationStandardFields(dto, new Compensation());
    }

    public Compensation convertToEntity(CompensationDto dto, Compensation comp) {
        Compensation compensation = buildCompensationStandardFields(dto, comp);
        compensation.setUpdatedAt(LocalDateTime.now());
        return compensation;
    }

    private Compensation buildCompensationStandardFields(CompensationDto dto, Compensation compensation) {
        compensation.setAmount(dto.getAmount());
        compensation.setEffectiveFrom(dto.getEffectiveFrom());
        compensation.setValidUntil(dto.getValidUntil());
        compensation.setSpecialConditions(dto.getSpecialConditions());
        return compensation;
    }

    public CompensationDto convertToDto(Compensation compensation) {
        return CompensationDto.builder()
                .id(compensation.getId())
                .amount(compensation.getAmount())
                .effectiveFrom(compensation.getEffectiveFrom())
                .validUntil(compensation.getValidUntil())
                .specialConditions(compensation.getSpecialConditions())
                .employeeId(compensation.getEmployee().getId())
                .previousCompensationId(findPreviousEmployeeCompensation(compensation))
                .nextCompensationId(findNextEmployeeCompensation(compensation))
                .build();
    }

    private Long findPreviousEmployeeCompensation(Compensation compensation) {
        List<Compensation> list = compensationRepository
                .findByNotBlockedAndIdLessThanAndEmployeeId(compensation.getId(),
                        compensation.getEmployee().getId());
        return  !list.isEmpty() ? list.get(list.size() - 1).getId() : null;
    }

    private Long findNextEmployeeCompensation(Compensation compensation) {
        List<Compensation> list = compensationRepository
                .findByNotBlockedAndIdGreaterThanAndEmployeeId(compensation.getId(),
                        compensation.getEmployee().getId());
        return !list.isEmpty()? list.get(0).getId() : null;
    }

    public CompensationShortViewDto convertToShortViewDto(Compensation compensation) {
        return CompensationShortViewDto.builder()
                .id(compensation.getId())
                .employeeFirstName(compensation.getEmployee().getUser().getFirstName())
                .employeeLastName(compensation.getEmployee().getUser().getLastName())
                .employeeId(compensation.getEmployee().getId())
                .effectiveFrom(compensation.getEffectiveFrom())
                .validUntil(compensation.getValidUntil())
                .updatedAt(compensation.getUpdatedAt())
                .amount(compensation.getAmount())
                .build();
    }
}
