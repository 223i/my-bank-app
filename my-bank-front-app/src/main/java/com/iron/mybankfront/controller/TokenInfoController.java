package com.iron.mybankfront.controller;

import com.iron.mybankfront.util.JwtTokenUtil;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TokenInfoController {

    private final JwtTokenUtil jwtTokenUtil;

    public TokenInfoController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/api/token-info")
    public Map<String, Object> getTokenInfo() {
        Map<String, Object> tokenInfo = new HashMap<>();
        
        tokenInfo.put("currentUser", jwtTokenUtil.getCurrentUserLogin());
        tokenInfo.put("userEmail", jwtTokenUtil.getCurrentUserEmail());
        tokenInfo.put("userRoles", jwtTokenUtil.getUserRoles());
        tokenInfo.put("hasAccountsAccess", jwtTokenUtil.hasAccountsAccess());
        tokenInfo.put("hasCashAccess", jwtTokenUtil.hasCashAccess());
        tokenInfo.put("hasTransferAccess", jwtTokenUtil.hasTransferAccess());
        tokenInfo.put("allClaims", jwtTokenUtil.getAllClaims());
        
        return tokenInfo;
    }
}
