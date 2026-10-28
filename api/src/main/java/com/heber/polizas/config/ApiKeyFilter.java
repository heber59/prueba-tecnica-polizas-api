package com.heber.polizas.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {
  private final String key;

  public ApiKeyFilter(@Value("${app.api-key}") String key) {
    this.key = key;
  }

  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String path = request.getServletPath();
    boolean publico =
        path.equals("/health")
            || path.equals("/swagger-ui.html")
            || path.startsWith("/swagger-ui/")
            || path.equals("/v3/api-docs")
            || path.startsWith("/v3/api-docs/");
    String provided = request.getHeader("x-api-key");
    if (!publico
        && (provided == null
            || !MessageDigest.isEqual(
                key.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8)))) {
      response.setStatus(401);
      response.setContentType("application/problem+json");
      response
          .getWriter()
          .write(
              "{\"status\":401,\"title\":\"Unauthorized\",\"detail\":\"API key ausente o"
                  + " incorrecta\"}");
      return;
    }
    chain.doFilter(request, response);
  }
}
