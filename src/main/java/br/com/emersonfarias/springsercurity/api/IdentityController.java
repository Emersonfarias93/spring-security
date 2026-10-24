package br.com.emersonfarias.springsercurity.api;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class IdentityController {

    @GetMapping("/public/hello")
    public MessageResponse hello() {
        return new MessageResponse("Olá! Este endpoint é público.");
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .sorted()
                .toList();

        return new CurrentUserResponse(authentication.getName(), roles);
    }

    public record MessageResponse(String message) {
    }

    public record CurrentUserResponse(String username, List<String> roles) {
    }
}
