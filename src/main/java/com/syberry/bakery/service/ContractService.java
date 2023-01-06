package com.syberry.bakery.service;

import com.syberry.bakery.dto.ContractFullDto;
import com.syberry.bakery.dto.ContractSaveDto;
import com.syberry.bakery.dto.ContractShortDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContractService {

    Page<ContractShortDto> getAllContracts(Pageable pageable, String name);

    ContractFullDto getByContractId(Long id);

    List<ContractShortDto> getByEmployeeId(Long id);

    ContractFullDto saveContract(ContractSaveDto dto);

    ContractFullDto updateContract(ContractSaveDto dto);

    void deleteContract(Long id);
}
