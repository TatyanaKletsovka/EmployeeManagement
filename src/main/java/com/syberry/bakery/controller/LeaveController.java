package com.syberry.bakery.controller;

import com.syberry.bakery.dto.LeaveFullDto;
import com.syberry.bakery.dto.LeaveSaveDto;
import com.syberry.bakery.dto.LeaveShortDto;
import com.syberry.bakery.dto.LeaveStatus;
import com.syberry.bakery.dto.LeaveType;
import com.syberry.bakery.service.LeaveService;
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
@RequestMapping("/leaves")
@RequiredArgsConstructor
@Slf4j
public class LeaveController {
    private final LeaveService leaveService;

    @GetMapping
    public Page<LeaveShortDto> getAllLeaves(Pageable pageable,
                                            @RequestParam(defaultValue = "") String name,
                                            @RequestParam(required = false) List<LeaveType> leaveTypes,
                                            @RequestParam(required = false) List<LeaveStatus> leaveStatuses) {
        log.info("GET-request: getting all leaves");
        return leaveService.getAll(pageable, name, leaveTypes, leaveStatuses);
    }

    @GetMapping("/employees/{id}")
    public List<LeaveShortDto> getLeavesByEmployeeId(@PathVariable("id") Long id) {
        log.info("GET-request: getting leaves for employee with id: {}", id);
        return leaveService.getByEmployeeId(id);
    }

    @GetMapping("/employees")
    public List<LeaveShortDto> getOwnedLeaves() {
        log.info("GET-request: getting leaves for employee(themself) with username: {}", getUserDetails().getUsername());
        return leaveService.getAllOwnedContracts();
    }

    @GetMapping("/{id}")
    public LeaveFullDto getLeaveById(@PathVariable("id") Long id) {
        log.info("GET-request: getting leave with id: {}", id);
        return leaveService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveFullDto addLeave(@Valid @RequestBody LeaveSaveDto dto) {
        log.info("POST-request: saving leave");
        return leaveService.save(dto);
    }

    @PutMapping("/{id}")
    public LeaveFullDto updateLeave(@PathVariable("id") Long id, @Valid @RequestBody LeaveSaveDto dto) {
        log.info("PUT-request: updating leave with id: {}", id);
        dto.setId(id);
        return leaveService.update(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeave(@PathVariable("id") Long id) {
        log.info("DELETE-request: deleting leave with id: {}", id);
        leaveService.delete(id);
    }
}
