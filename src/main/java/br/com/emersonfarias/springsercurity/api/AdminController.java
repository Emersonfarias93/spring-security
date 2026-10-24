package br.com.emersonfarias.springsercurity.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    @GetMapping("/hello")
    public AdminMessageResponse hello() {
        return new AdminMessageResponse("Olá! Acesso administrativo autorizado.");
    }

    public record AdminMessageResponse(String message) {
    }
}
