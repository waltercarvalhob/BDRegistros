package br.com.bdregistros.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Quando o frontend Angular e empacotado dentro do backend (ver
 * pom.xml/bundle-frontend), o navegador pode navegar direto para uma rota
 * client-side (ex.: recarregar a pagina em /titulares). Sem esse fallback,
 * o Spring devolveria 404 por nao existir esse caminho no servidor; aqui
 * ele encaminha para o index.html e o Angular Router assume dali.
 */
@Controller
public class SpaFallbackController {

    @GetMapping({"/login", "/titulares", "/usuarios", "/backup"})
    public String encaminharParaIndex() {
        return "forward:/index.html";
    }
}
