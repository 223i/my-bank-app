package com.iron.mybankfront.service;

import com.iron.mybankfront.controller.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class GatewayService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String GATEWAY_URL = "http://gateway-service/api";

    /**
     * Получить данные текущего пользователя. Login не нужен в URL — accounts-service
     * определяет пользователя по JWT-токену (@AuthenticationPrincipal).
     */
    public AccountDto getAccountInfo(String userLogin) {
        log.debug("Fetching account info for user: {}", userLogin);
        return restTemplate.getForObject(GATEWAY_URL + "/accounts", AccountDto.class);
    }

    public AccountDto changeAccountInfo(AccountUpdateDto dataToUpdate) {
        log.debug("Updating account info");
        return restTemplate.postForObject(GATEWAY_URL + "/accounts", dataToUpdate, AccountDto.class);
    }

    /**
     * Операция с наличными. После выполнения запрашиваем актуальное состояние аккаунта,
     * так как cash-service возвращает void (не знает о структуре AccountDto).
     */
    public AccountDto changeCashInfo(int value, CashAction action) {
        log.debug("Cash operation: type={}, amount={}", action, value);
        String url = GATEWAY_URL + "/cash/operation?value=" + value + "&type=" + action.name();
        restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Void.class);
        return getAccountInfo(null);
    }

    /**
     * Перевод средств. После выполнения запрашиваем актуальное состояние аккаунта.
     */
    public AccountDto transfer(int value, String recipientLogin) {
        log.debug("Transfer to {}, amount={}", recipientLogin, value);
        String url = GATEWAY_URL + "/transfer?value=" + value + "&login=" + recipientLogin;
        restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Void.class);
        return getAccountInfo(null);
    }
}