package com.syberry.bakery.util;

import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class SecurityContextUtil {
    public static UserDetailsImpl getUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static boolean hasAuthority(RoleName roleName) {
        return getUserDetails().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(roleName.name()));
    }

    public static boolean hasAnyAuthority(List<RoleName> roleNames) {
        return getUserDetails().getAuthorities()
                .stream().anyMatch(a -> roleNames.stream().anyMatch(r -> r.name().equals(a.getAuthority())));
        }
}
