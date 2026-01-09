package com.example.PlataformaDarcy.config;

import com.example.PlataformaDarcy.model.Usuario;
import com.example.PlataformaDarcy.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    @Lazy // Evita dependência circular se houver
    private AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            // Extrair dados do Google
            String email = oauthToken.getPrincipal().getAttribute("email");
            String nome = oauthToken.getPrincipal().getAttribute("name");

            // Processar login/registro
            Usuario usuario = authService.processarLoginGoogle(email, nome);

            // Atualizar contexto de segurança com nosso Usuario
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    usuario,
                    null,
                    usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Se for o primeiro acesso (revisar lógica se quiser redirecionar para
            // completar cadastro),
            // mas por enquanto vai pra home.
            response.sendRedirect("/");
        } else {
            response.sendRedirect("/");
        }
    }
}
