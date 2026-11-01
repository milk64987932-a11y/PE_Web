package com.umc.movie.controller;

import com.umc.movie.dto.*;
import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Validated
public class MemberController {
  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<ApiResponse<MemberResponse>> signUp(
      @Valid @RequestBody SignUpRequest request) {
    return ResponseEntity.status(201)
        .body(ApiResponse.created("회원가입에 성공했습니다.", memberService.signUp(request)));
  }

  @GetMapping("/nickname/{nickname}")
  public NicknameCheckResponse checkNickname(
      @PathVariable @NotBlank @Size(max = 50) String nickname) {
    return memberService.checkNickname(nickname);
  }

  @GetMapping("/email/{email}")
  public EmailCheckResponse checkEmail(
      @PathVariable @NotBlank @Email @Size(max = 254) String email) {
    return memberService.checkEmail(email);
  }
}
