package com.umc.movie.dto;

import com.umc.movie.entity.Member;
import java.time.LocalDateTime;

public record MyMemberResponse(String email, String nickname, LocalDateTime createdAt) {
  public static MyMemberResponse from(Member member) {
    return new MyMemberResponse(member.getEmail(), member.getNickname(), member.getCreatedAt());
  }
}
