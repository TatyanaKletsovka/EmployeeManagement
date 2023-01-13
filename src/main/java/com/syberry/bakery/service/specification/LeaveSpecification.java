package com.syberry.bakery.service.specification;

import com.syberry.bakery.dto.LeaveStatus;
import com.syberry.bakery.dto.LeaveType;
import com.syberry.bakery.entity.Employee_;
import com.syberry.bakery.entity.Leave;
import com.syberry.bakery.entity.Leave_;
import com.syberry.bakery.entity.User_;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import java.util.List;

@UtilityClass
public class LeaveSpecification {

    public Specification<Leave> checkName(String name) {
        return (((root, query, criteriaBuilder) -> {
            Expression<String> getFullName = criteriaBuilder
                    .concat(criteriaBuilder.concat(
                                    root.get(Leave_.EMPLOYEE).get(Employee_.USER).get(User_.FIRST_NAME), " "),
                            root.get(Leave_.EMPLOYEE).get(Employee_.USER).get(User_.LAST_NAME));
            return criteriaBuilder.like(criteriaBuilder.lower(getFullName), "%" + name + "%");
        }));
    }

    public Specification<Leave> isBlockedFalse() {
        return (((root, query, criteriaBuilder) -> criteriaBuilder
                .equal(root.get(Leave_.EMPLOYEE).get(Employee_.USER).get(User_.IS_BLOCKED), false)));
    }

    public Specification<Leave> checkLeaveType(List<LeaveType> leaveTypes) {
        return (((root, query, criteriaBuilder) -> leaveTypes != null
                ? criteriaBuilder.in(root.get(Leave_.LEAVE_TYPE)).value(leaveTypes)
                : criteriaBuilder.isNotNull(root.get(Leave_.LEAVE_TYPE))));
    }

    public Specification<Leave> checkLeaveStatus(List<LeaveStatus> leaveStatuses) {
        return (((root, query, criteriaBuilder) -> leaveStatuses != null
                ? criteriaBuilder.in(root.get(Leave_.LEAVE_STATUS)).value(leaveStatuses)
                : criteriaBuilder.isNotNull(root.get(Leave_.LEAVE_STATUS))));
    }

    public Specification<Leave> forNameTypeStatus(String name, List<LeaveType> leaveTypes, List<LeaveStatus> leaveStatuses) {
        return isBlockedFalse()
                .and(checkName(name))
                .and(checkLeaveType(leaveTypes))
                .and(checkLeaveStatus(leaveStatuses));
    }
}
