package com.studyhelper.config;

import com.studyhelper.entity.User;

public record UserPrincipal(Long id, String username, User.Role role) {
}
