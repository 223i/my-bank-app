package com.iron.mybankfront.controller;

import com.iron.mybankfront.controller.dto.AccountDto;
import com.iron.mybankfront.controller.dto.AccountUpdateDto;
import com.iron.mybankfront.controller.dto.CashAction;
import com.iron.mybankfront.service.GatewayService;
import com.iron.mybankfront.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

/**
 * Контроллер main.html.
 * <p>
 * Используемая модель для main.html:
 * model.addAttribute("name", name);
 * model.addAttribute("birthdate", birthdate.format(DateTimeFormatter.ISO_DATE));
 * model.addAttribute("sum", sum);
 * model.addAttribute("accounts", accounts);
 * model.addAttribute("errors", errors);
 * model.addAttribute("info", info);
 * <p>
 * Поля модели:
 * name - Фамилия Имя текущего пользователя, String (обязательное)
 * birthdate - дата рождения текущего пользователя, String в формате 'YYYY-MM-DD' (обязательное)
 * sum - сумма на счету текущего пользователя, Integer (обязательное)
 * accounts - список аккаунтов, которым можно перевести деньги, List<AccountDto> (обязательное)
 * errors - список ошибок после выполнения действий, List<String> (не обязательное)
 * info - строка успешности после выполнения действия, String (не обязательное)
 * <p>
 * С примерами использования можно ознакомиться в тестовом классе заглушке AccountStub
 */
@Slf4j
@Controller
public class MainController {

    private final GatewayService gatewayService;
    private final JwtTokenUtil jwtTokenUtil;

    public MainController(GatewayService gatewayService, JwtTokenUtil jwtTokenUtil) {
        this.gatewayService = gatewayService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * GET /.
     * Редирект на GET /account
     */
    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(Model model) {
        log.debug("Get account page for login {}", jwtTokenUtil.getCurrentUserLogin());
        String login = jwtTokenUtil.getCurrentUserLogin();
        Collection<String> userRoles = jwtTokenUtil.getUserRoles();

        AccountDto accountDto = gatewayService.getAccountInfo(login);
        model.addAttribute("name", accountDto.name());
        model.addAttribute("birthdate", accountDto.birthday().format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("sum", accountDto.sum());
        model.addAttribute("accounts", accountDto.accounts());

        // Add JWT token information to model
        model.addAttribute("currentUser", login);
        model.addAttribute("userEmail", jwtTokenUtil.getCurrentUserEmail());
        model.addAttribute("userRoles", userRoles);
        model.addAttribute("hasAccountsAccess", jwtTokenUtil.hasAccountsAccess());
        model.addAttribute("hasCashAccess", jwtTokenUtil.hasCashAccess());
        model.addAttribute("hasTransferAccess", jwtTokenUtil.hasTransferAccess());
        model.addAttribute("jwtToken", jwtTokenUtil.getJwtToken());

        return "main";
    }

    /**
     * POST /account.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для изменения данных текущего пользователя по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     * <p>
     * Изменяемые данные:
     * 1. name - Фамилия Имя
     * 2. birthdate - дата рождения в формате YYYY-DD-MM
     */
    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam("name") String name,
            @RequestParam("birthdate") LocalDate birthdate
    ) {

        AccountUpdateDto toBeUpdated = new AccountUpdateDto(name, birthdate);
        AccountDto updated = gatewayService.changeAccountInfo(toBeUpdated);
        model.addAttribute("name", updated.name());
        model.addAttribute("birthdate", updated.birthday().format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("sum", updated.sum());
        model.addAttribute("accounts", updated.accounts());

        model.addAttribute("currentUser", jwtTokenUtil.getCurrentUserLogin());
        model.addAttribute("userEmail", jwtTokenUtil.getCurrentUserEmail());
        model.addAttribute("userRoles", jwtTokenUtil.getUserRoles());
        model.addAttribute("hasAccountsAccess", jwtTokenUtil.hasAccountsAccess());
        model.addAttribute("hasCashAccess", jwtTokenUtil.hasCashAccess());
        model.addAttribute("hasTransferAccess", jwtTokenUtil.hasTransferAccess());

        return "main";
    }

    /**
     * POST /cash.
     * Что нужно сделать:
     * 1. Сходить в сервис cash через Gateway API для снятия/пополнения счета текущего аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     * <p>
     * Параметры:
     * 1. value - сумма списания
     * 2. action - GET (снять), PUT (пополнить)
     */
    @PostMapping("/cash")
    public String editCash(Model model, @RequestParam("value") int value, @RequestParam("action") CashAction action) {
        String login = jwtTokenUtil.getCurrentUserLogin();

        try {
            AccountDto updated = gatewayService.changeCashInfo(value, action);
            model.addAttribute("name", updated.name());
            model.addAttribute("birthdate", updated.birthday().format(DateTimeFormatter.ISO_DATE));
            model.addAttribute("sum", updated.sum());
            model.addAttribute("accounts", updated.accounts());
            model.addAttribute("info", action == CashAction.GET ? "Снято %d руб".formatted(value) : "Положено %d руб".formatted(value));
        } catch (Exception e) {
            AccountDto current = gatewayService.getAccountInfo(login);
            model.addAttribute("name", current.name());
            model.addAttribute("birthdate", current.birthday().format(DateTimeFormatter.ISO_DATE));
            model.addAttribute("sum", current.sum());
            model.addAttribute("accounts", current.accounts());
            model.addAttribute("errors", List.of("Недостаточно средств на счету"));
        }

        model.addAttribute("currentUser", login);
        model.addAttribute("userEmail", jwtTokenUtil.getCurrentUserEmail());
        model.addAttribute("userRoles", jwtTokenUtil.getUserRoles());
        model.addAttribute("hasAccountsAccess", jwtTokenUtil.hasAccountsAccess());
        model.addAttribute("hasCashAccess", jwtTokenUtil.hasCashAccess());
        model.addAttribute("hasTransferAccess", jwtTokenUtil.hasTransferAccess());

        return "main";
    }

    /**
     * POST /transfer.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для перевода со счета текущего аккаунта на счет другого аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     * <p>
     * Параметры:
     * 1. value - сумма списания
     * 2. login - логин пользователя получателя
     */
    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("login") String login
    ) {
        String currentLogin = jwtTokenUtil.getCurrentUserLogin();

        try {
            AccountDto updated = gatewayService.transfer(value, login);
            model.addAttribute("name", updated.name());
            model.addAttribute("birthdate", updated.birthday().format(DateTimeFormatter.ISO_DATE));
            model.addAttribute("sum", updated.sum());
            model.addAttribute("accounts", updated.accounts());
            model.addAttribute("info", "Успешно переведено %d руб клиенту %s".formatted(value, login));
        } catch (Exception e) {
            AccountDto current = gatewayService.getAccountInfo(currentLogin);
            model.addAttribute("name", current.name());
            model.addAttribute("birthdate", current.birthday().format(DateTimeFormatter.ISO_DATE));
            model.addAttribute("sum", current.sum());
            model.addAttribute("accounts", current.accounts());
            model.addAttribute("errors", List.of("Недостаточно средств на счету"));
        }

        // Add JWT token information to model
        model.addAttribute("currentUser", currentLogin);
        model.addAttribute("userEmail", jwtTokenUtil.getCurrentUserEmail());
        model.addAttribute("userRoles", jwtTokenUtil.getUserRoles());
        model.addAttribute("hasAccountsAccess", jwtTokenUtil.hasAccountsAccess());
        model.addAttribute("hasCashAccess", jwtTokenUtil.hasCashAccess());
        model.addAttribute("hasTransferAccess", jwtTokenUtil.hasTransferAccess());

        return "main";
    }
}
