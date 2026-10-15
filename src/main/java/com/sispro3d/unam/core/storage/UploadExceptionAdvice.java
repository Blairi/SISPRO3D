package com.sispro3d.unam.core.storage;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Convierte el rechazo del contenedor por tamaño excedido en un mensaje en
 * español sobre el formulario, en lugar de un error 500.
 */
@ControllerAdvice
public class UploadExceptionAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(HttpServletRequest request, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", "El archivo no debe superar los 100 MB");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/expert/orders");
    }
}
