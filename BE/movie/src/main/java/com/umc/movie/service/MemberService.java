package com.umc.movie.service;

import com.umc.movie.dto.*;
import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final com.umc.movie.config.JwtProvider jwtProvider;

  public LoginResponse login(LoginRequest request) {
    Member member = memberRepository.findByEmail(request.email()).orElse(null);
    if (member == null || request.password().getBytes(StandardCharsets.UTF_8).length > 72
        || !passwordEncoder.matches(request.password(), member.getPassword()))
      throw new ApiException(401, "AUTH401_1", "이메일 또는 비밀번호가 올바르지 않습니다.");
    return new LoginResponse(member.getNickname(), jwtProvider.create(member.getMemberId()));
  }

  public MyMemberResponse me(Long memberId) {
    return MyMemberResponse.from(findMember(memberId));
  }

  @Transactional
  public MyMemberResponse updateMe(Long memberId, UpdateMemberRequest request) {
    Member member = findMember(memberId);
    if (memberRepository.existsByNicknameAndMemberIdNot(request.nickname(), memberId))
      throw new ApiException(409, "MEMBER409_1", "이미 사용 중인 닉네임입니다.");
    member.updateNickname(request.nickname());
    try {
      memberRepository.flush();
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      throw new ApiException(409, "MEMBER409_1", "이미 사용 중인 닉네임입니다.");
    }
    return MyMemberResponse.from(member);
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new ApiException(404, "MEMBER404_1", "존재하지 않는 회원입니다."));
  }

  @Transactional
  public MemberResponse signUp(SignUpRequest request) {
    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ApiException(400, "COMMON400", "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
    if (memberRepository.existsByEmail(request.email()))
      throw new ApiException(409, "MEMBER4091", "이미 사용 중인 이메일입니다.");
    if (memberRepository.existsByNickname(request.nickname()))
      throw new ApiException(409, "MEMBER4092", "이미 사용 중인 닉네임입니다.");
    Member member =
        memberRepository.saveAndFlush(
            new Member(
                request.email(), passwordEncoder.encode(request.password()), request.nickname()));
    return new MemberResponse(member.getMemberId(), member.getEmail(), member.getNickname());
  }

  public NicknameCheckResponse checkNickname(String nickname) {
    return new NicknameCheckResponse(!memberRepository.existsByNickname(nickname));
  }

  public EmailCheckResponse checkEmail(String email) {
    return new EmailCheckResponse(!memberRepository.existsByEmail(email));
  }
}
