package com.syberry.bakery.service;

import com.syberry.bakery.dto.LeaveFullDto;
import com.syberry.bakery.dto.LeaveSaveDto;
import com.syberry.bakery.dto.LeaveShortDto;
import com.syberry.bakery.dto.LeaveStatus;
import com.syberry.bakery.dto.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeaveService {

    Page<LeaveShortDto> getAll(Pageable pageable, String name, List<LeaveType> leaveTypes, List<LeaveStatus> leaveStatuses);

    List<LeaveShortDto> getByEmployeeId(Long id);

    List<LeaveShortDto> getAllOwnedContracts();

    LeaveFullDto getById(Long id);

    LeaveFullDto save(LeaveSaveDto dto);

    LeaveFullDto update(LeaveSaveDto dto);

    void delete(Long id);

}
