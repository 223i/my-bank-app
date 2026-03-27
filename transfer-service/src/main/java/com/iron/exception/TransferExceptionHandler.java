package com.iron.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class TransferExceptionHandler {

    @ExceptionHandler(TransferException.class)
    public String handleTransferError(TransferException ex, RedirectAttributes redirectAttributes) {
        // Добавляем сообщение об ошибке, которое можно показать в Thymeleaf
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/transfer-error"; // Страница, где выведем текст ошибки
    }
}
