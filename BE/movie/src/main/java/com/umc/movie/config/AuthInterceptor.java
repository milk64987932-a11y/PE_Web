package com.umc.movie.config;

import com.umc.movie.exception.ApiException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
  private final JwtProvider jwtProvider;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String method = request.getMethod();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    if (method.equals("OPTIONS")
        || (method.equals("POST") && (path.equals("/auth/login") || path.equals("/members")))
        || (method.equals("GET") && (path.matches("/members/(email|nickname)/[^/]+")
            || path.matches("/members/[0-9]+/ratings")))) return true;
    String header = request.getHeader("Authorization");
    if (header == null || header.isBlank())
      throw new ApiException(401, "AUTH401_2", "인증 토큰이 필요합니다.");
    if (!header.startsWith("Bearer ") || header.substring(7).isBlank())
      throw new ApiException(401, "AUTH401_3", "유효하지 않은 토큰입니다.");
    request.setAttribute("memberId", jwtProvider.verify(header.substring(7)));
    return true;
  }
}
