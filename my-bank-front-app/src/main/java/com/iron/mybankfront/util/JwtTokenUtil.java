package com.iron.mybankfront.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenUtil {

    public String getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("preferred_username");
        }
        return authentication != null ? authentication.getName() : null;
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute(StandardClaimNames.EMAIL);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            Object roles = oauth2User.getAttribute("roles");
            if (roles instanceof Collection) {
                return (Collection<String>) roles;
            }
        }
        return List.of();
    }

    public Map<String, Object> getAllClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            return oauth2User.getAttributes();
        }
        return Map.of();
    }

    public boolean hasRole(String role) {
        Collection<String> roles = getUserRoles();
        return roles.contains(role) || roles.contains("ROLE_" + role);
    }

    public boolean hasAccountsAccess() {
        return hasRole("ROLE_ACCOUNTS_INTERNAL") || hasRole("ACCOUNTS_INTERNAL");
    }

    public boolean hasCashAccess() {
        return hasRole("ROLE_CASH_USER") || hasRole("CASH_USER");
    }

    public boolean hasTransferAccess() {
        return hasRole("ROLE_TRANSFER_USER") || hasRole("TRANSFER_USER");
    }

}
