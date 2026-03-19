package com.studyhelper.config;

import com.studyhelper.entity.User;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AccessGuard {

    public UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("未登录或登录已过期");
        }
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("未登录或登录已过期");
        }
        return principal;
    }

    public void requireSelfOrAdmin(Long userId) {
        UserPrincipal principal = currentUser();
        if (!Objects.equals(principal.id(), userId) && principal.role() != User.Role.ADMIN) {
            throw new AccessDeniedException("无权访问该用户数据");
        }
    }

    public void requireRole(User.Role... roles) {
        User.Role currentRole = currentUser().role();
        for (User.Role role : roles) {
            if (currentRole == role) {
                return;
            }
        }
        throw new AccessDeniedException("无权执行该操作");
    }
}
