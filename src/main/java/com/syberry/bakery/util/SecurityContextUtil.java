package com.syberry.bakery.util;

import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextUtil {
    public static UserDetailsImpl getUserDetails() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static boolean hasAuthority(RoleName roleName) {
        return getUserDetails().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(roleName.name()));
    }

}
