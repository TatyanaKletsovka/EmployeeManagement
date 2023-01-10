package com.syberry.bakery.service.specification;

import com.syberry.bakery.dto.CompensationFilterDto;
import com.syberry.bakery.entity.Compensation;
import com.syberry.bakery.entity.Compensation_;
import com.syberry.bakery.entity.Employee_;
import com.syberry.bakery.entity.User_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CompensationSpecification {

    public Specification<Compensation> buildGetAllSpecification(CompensationFilterDto filter) {
        return buildIsBlockedFalseSpecification()
                .and(buildNameLikeSpecification(filter.getName()))
                .and(buildAmountBetweenSpecification(filter.getAmountStart(), filter.getAmountEnd()))
                .and(buildEffectiveFromBetweenSpecification(filter.getEffectiveFromStart(), filter.getEffectiveFromEnd()))
                .and(buildValidUntilBetweenSpecification(filter.getValidUntilStart(), filter.getValidUntilEnd()))
                .and(buildUpdatedAtSpecification(filter.getIsUpdated(), filter.getUpdatedAtStart().atStartOfDay(),
                        filter.getUpdatedAtEnd().plusDays(1).atStartOfDay()));
    }

    public Specification<Compensation> buildGetAllByEmployeeIdSpecification(Long id, CompensationFilterDto filter) {
        return buildWhereEmployeeIdIsSpecification(id).and(buildGetAllSpecification(filter));
    }

    private Specification<Compensation> buildWhereEmployeeIdIsSpecification(Long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Compensation_.EMPLOYEE)
                .get(Employee_.ID), id);
    }

    private Specification<Compensation> buildIsBlockedFalseSpecification() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Compensation_.EMPLOYEE)
                .get(Employee_.USER).get(User_.IS_BLOCKED), false);
    }

    private Specification<Compensation> buildNameLikeSpecification(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                    criteriaBuilder.concat(
                            root.get(Compensation_.EMPLOYEE).get(Employee_.USER).get(User_.FIRST_NAME) + " ",
                            root.get(Compensation_.EMPLOYEE).get(Employee_.USER).get(User_.LAST_NAME)),
                    "%" + name + "%");
    }

    private Specification<Compensation> buildAmountBetweenSpecification(float amountStart, float amountEnd) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(Compensation_.AMOUNT), amountStart, amountEnd);
    }

    private Specification<Compensation> buildEffectiveFromBetweenSpecification(LocalDate effectiveFromStart, LocalDate effectiveFromEnd) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(Compensation_.EFFECTIVE_FROM), effectiveFromStart, effectiveFromEnd);
    }

    private Specification<Compensation> buildValidUntilBetweenSpecification(LocalDate validUntilStart, LocalDate validUntilEnd) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(Compensation_.VALID_UNTIL), validUntilStart, validUntilEnd);
    }

    private Specification<Compensation> buildUpdatedAtSpecification(Boolean isUpdated, LocalDateTime updatedAtStart,
                                                                    LocalDateTime updatedAtEnd) {
        if (isUpdated == null){
            return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get(Compensation_.UPDATED_AT)),
                    criteriaBuilder.between(root.get(Compensation_.UPDATED_AT), updatedAtStart, updatedAtEnd));
        } else if (isUpdated) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get(Compensation_.UPDATED_AT)),
                        criteriaBuilder.between(root.get(Compensation_.UPDATED_AT), updatedAtStart, updatedAtEnd));
        } else {
            return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(Compensation_.UPDATED_AT));
        }
    }
}
