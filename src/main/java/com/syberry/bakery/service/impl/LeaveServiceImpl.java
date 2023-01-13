package com.syberry.bakery.service.impl;

import com.syberry.bakery.converter.LeaveConverter;
import com.syberry.bakery.dto.LeaveFullDto;
import com.syberry.bakery.dto.LeaveSaveDto;
import com.syberry.bakery.dto.LeaveShortDto;
import com.syberry.bakery.dto.LeaveStatus;
import com.syberry.bakery.dto.LeaveType;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.entity.Leave;
import com.syberry.bakery.exception.AccessException;
import com.syberry.bakery.exception.CreateException;
import com.syberry.bakery.exception.DeleteException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.exception.UpdateException;
import com.syberry.bakery.repository.EmployeeRepository;
import com.syberry.bakery.repository.LeaveRepository;
import com.syberry.bakery.service.LeaveService;
import com.syberry.bakery.service.specification.LeaveSpecification;
import com.syberry.bakery.util.LeaveUtil;
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
public class LeaveServiceImpl implements LeaveService {
    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveUtil leaveUtil;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT')")
    public Page<LeaveShortDto> getAll(Pageable pageable, String name, List<LeaveType> leaveTypes, List<LeaveStatus> leaveStatuses) {
        return leaveRepository.findAll(LeaveSpecification.forNameTypeStatus(name, leaveTypes, leaveStatuses), pageable)
                .map(LeaveConverter::toShortDto);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT')")
    public List<LeaveShortDto> getByEmployeeId(Long id) {
        return leaveRepository
                .findByEmployeeIdAndEmployeeUserIsBlockedFalse(id)
                .stream().map(LeaveConverter::toShortDto)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public List<LeaveShortDto> getAllOwnedContracts() {
        return leaveRepository.findByEmployeeUserEmailAndEmployeeUserIsBlockedFalse(getUserDetails().getUsername())
                .stream().map(LeaveConverter::toShortDto)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public LeaveFullDto getById(Long id) {
        Leave leave = leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no such leave"));
        if (hasAnyAuthority(List.of(RoleName.ROLE_ADMIN, RoleName.ROLE_HR, RoleName.ROLE_ACCOUNTANT)) ||
                (hasAuthority(RoleName.ROLE_USER)
                        && Objects.equals(getEmailByLeave(leave), getUserDetails().getUsername()))) {
            return LeaveConverter.toFullDto(leave, leaveUtil);
        }
        throw new AccessException("Viewing other people's leaves is prohibited");
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public LeaveFullDto save(LeaveSaveDto dto) {
        Leave leave = LeaveConverter.toEntity(dto, employeeRepository);
        if (hasAuthority(RoleName.ROLE_ADMIN) ||
                (hasAnyAuthority(List.of(RoleName.ROLE_USER, RoleName.ROLE_HR, RoleName.ROLE_ACCOUNTANT))
                        && Objects.equals(getEmailByLeave(leave), getUserDetails().getUsername()))) {
            validateLeave(leave);
            return LeaveConverter.toFullDto(leaveRepository.save(leave), leaveUtil);
        }
        throw new CreateException("You can create leave only for yourself");
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public LeaveFullDto update(LeaveSaveDto dto) {
        Leave leave = leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("There is no such leave"));
        leave.setEmployee(employeeRepository.findByIdAndUserIsBlockedFalse(dto.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("There is no such employee")));
        if (Objects.equals(getUserDetails().getUsername(), getEmailByLeave(leave))) {
            throw new UpdateException("You can't update leave for yourself");
        }
        leave.setLeaveType(dto.getLeaveType());
        leave.setLeaveStartDate(dto.getLeaveStartDate());
        leave.setLeaveEndDate(dto.getLeaveEndDate());
        leave.setLeaveStatus(dto.getLeaveStatus());
        leave.setLeaveReason(dto.getLeaveReason());
        leave.setUpdatedAt(LocalDateTime.now());
        validateLeave(leave);
        return getById(leave.getId());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void delete(Long id) {
        Optional<Leave> leave = leaveRepository.findByIdAndEmployeeUserIsBlockedFalse(id);
        if (leave.isPresent()) {
            if (Objects.equals(getEmailByLeave(leave.get()), getUserDetails().getUsername())) {
                throw new DeleteException("You can't delete your contract");
            }
            leaveRepository.deleteById(id);
        } else {
            throw new DeleteException("There is no such leave");
        }
    }

    private void validateLeave(Leave leave) {
        Optional<Leave> forCheckData = leaveRepository.findByEmployeeIdAndLeaveStartDateOrLeaveEndDateBetween(leave.getEmployee().getId(),
                leave.getLeaveStartDate(), leave.getLeaveEndDate());
        if (forCheckData.isPresent() && !Objects.equals(forCheckData.get().getId(), leave.getId())) {
            throw new CreateException("You already have planned leave on this days");
        }
        if (leave.getLeaveType() == LeaveType.SICK_DAY) {
            if (!leaveUtil.checkRemainingDays(leave, LeaveType.SICK_DAY)) {
                throw new CreateException("You don't have enough sick days");
            }
        } else if (leave.getLeaveType() == LeaveType.PAID_LEAVE) {
            if (!leaveUtil.checkRemainingDays(leave, LeaveType.PAID_LEAVE)) {
                throw new CreateException("You don't have enough paid leave days");
            }
        }
    }

    private static String getEmailByLeave(Leave leave) {
        return leave.getEmployee().getUser().getEmail();
    }
}
