package com.iron.mybankfront.service;

import com.iron.mybankfront.controller.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class GatewayService {

    private final RestTemplate restTemplate;
    private final String gatewayUrl;

    public GatewayService(RestTemplate restTemplate,
                          @Value("${gateway.url:http://gateway-service/api}") String gatewayUrl) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = gatewayUrl;
    }

    /**
     * Получить данные текущего пользователя. Login не нужен в URL — accounts-service
     * определяет пользователя по JWT-токену (@AuthenticationPrincipal).
     */
    public AccountDto getAccountInfo(String userLogin) {
        log.debug("Fetching account info for user: {}", userLogin);
        return restTemplate.getForObject(gatewayUrl + "/accounts", AccountDto.class);
    }

    public AccountDto changeAccountInfo(AccountUpdateDto dataToUpdate) {
        log.debug("Updating account info");
        return restTemplate.postForObject(gatewayUrl + "/accounts", dataToUpdate, AccountDto.class);
    }

    /**
     * Операция с наличными. После выполнения запрашиваем актуальное состояние аккаунта,
     * так как cash-service возвращает void (не знает о структуре AccountDto).
     */
    public AccountDto changeCashInfo(int value, CashAction action) {
        log.debug("Cash operation: type={}, amount={}", action, value);
        String url = gatewayUrl + "/cash/operation?value=" + value + "&type=" + action.name();
        restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Void.class);
        return getAccountInfo(null);
    }

    /**
     * Перевод средств. После выполнения запрашиваем актуальное состояние аккаунта.
     */
    public AccountDto transfer(int value, String recipientLogin) {
        log.debug("Transfer to {}, amount={}", recipientLogin, value);
        String url = gatewayUrl + "/transfer?value=" + value + "&login=" + recipientLogin;
        restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Void.class);
        return getAccountInfo(null);
    }
}
