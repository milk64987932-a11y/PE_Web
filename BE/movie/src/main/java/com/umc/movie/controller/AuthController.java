package com.umc.movie.controller;

import com.umc.movie.dto.*;
import com.umc.movie.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
  private final MemberService memberService;

  @PostMapping("/auth/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.ok("로그인에 성공했습니다.", memberService.login(request));
  }
}
