package com.ll.security;

import com.ll.model.PersonalAccount;
import com.ll.service.PersonalAccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
  private static final String API_KEY_HEADER = "x-api-key";

  private final JwtService jwtService;
  private final PersonalAccountService personalAccountService;

  public ApiKeyAuthFilter(JwtService jwtService, PersonalAccountService personalAccountService) {
    this.jwtService = jwtService;
    this.personalAccountService = personalAccountService;
  }

  public static class ExcludedPath {
    private final String path;
    private final String method;

    public ExcludedPath(String path, String method) {
      this.path = path;
      this.method = method;
    }
  }

  private List<ExcludedPath> excludedPaths = new ArrayList<>();

  public void setExcludedPaths(List<ExcludedPath> paths) {
    this.excludedPaths = paths;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    String method = request.getMethod();

    return excludedPaths.stream()
        .anyMatch(excluded -> {
          if (excluded.path.contains("/**")) {
            // For paths with wildcards, check if the request path starts with the base path
            String basePath = excluded.path.substring(0, excluded.path.indexOf("/**"));
            boolean pathMatches = path.startsWith(basePath);
            boolean methodMatches = excluded.method == null || excluded.method.equals(method);
            return pathMatches && methodMatches;
          } else {
            // For exact paths, do exact matching
            boolean exactMatch = path.equals(excluded.path);
            boolean methodMatches = excluded.method == null || excluded.method.equals(method);
            return exactMatch && methodMatches;
          }
        });
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    String token = request.getHeader(API_KEY_HEADER);

    // System.out.println("Request URL: " + request.getRequestURL());
    // System.out.println("Token received: " + (token != null ? "present" :
    // "null"));

    if (token == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"error\": \"Missing API key\"}");
      return;
    }

    try {
      Claims claims = jwtService.validateToken(token);
      String accountId = claims.getSubject();

      // System.out.println("Token validated, account ID: " + accountId);

      Optional<PersonalAccount> accountOpt = personalAccountService.getPersonalAccount(accountId);
      if (accountOpt.isEmpty()) {
        // System.out.println("Account not found for ID: " + accountId);
        throw new Exception("Invalid account");
      }

      PersonalAccount account = accountOpt.get();
      // System.out.println("Found account: " + account.getId());

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          account, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);
    } catch (Exception e) {
      // System.out.println("Authentication failed: " + e.getMessage());
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"error\": \"Invalid API key\"}");
    }
  }
}
