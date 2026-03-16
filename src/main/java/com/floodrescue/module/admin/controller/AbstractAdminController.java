package com.floodrescue.module.admin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

abstract class AbstractAdminController {

    protected Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
