package com.iron.mybankfront.service;

import com.iron.mybankfront.controller.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GatewayService {

    @Autowired
    private RestTemplate restTemplate;

    private final String GATEWAY_URL = "http://gateway-service/api";

    public AccountDto getAccountInfo(String userLogin) {
        String url = GATEWAY_URL + "/accounts/" + userLogin;
        return restTemplate.getForObject(url, AccountDto.class);
    }

    public AccountDto changeAccountInfo(AccountUpdateDto dataToUpdate) {
        String url = GATEWAY_URL + "/accounts";
        return restTemplate.postForObject(url, dataToUpdate, AccountDto.class);
    }

    public AccountDto changeCashInfo(int value, CashAction action) {
        String url = GATEWAY_URL + "/cash";
        CashRequestDto request = new CashRequestDto(value, action);
        return restTemplate.postForObject(url, request, AccountDto.class);
    }

    public AccountDto transfer(int value, String login) {
        String url = GATEWAY_URL + "/transfer";
        TransferRequestDto request = new TransferRequestDto(value, login);
        return restTemplate.postForObject(url, request, AccountDto.class);
    }

}
