package com.umc.movie.service;

import com.umc.movie.dto.*;
import com.umc.movie.entity.*;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {
  private final MemberRepository memberRepository;
  private final RatingRepository ratingRepository;

  @Transactional
  public Long create(Long memberId, CreateRatingRequest request) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new ApiException(404, "MEMBER404", "존재하지 않는 회원입니다."));
    if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId()))
      throw new ApiException(409, "RATING4091", "이미 이 영화에 평점을 남기셨습니다.");
    return ratingRepository
        .saveAndFlush(new Rating(member, request.movieId(), request.score(), request.comment()))
        .getRatingId();
  }

  @Transactional
  public void update(Long ratingId, Long memberId, UpdateRatingRequest request) {
    findOwned(ratingId, memberId).update(request.score(), request.comment());
  }

  @Transactional
  public void delete(Long ratingId, Long memberId) {
    ratingRepository.delete(findOwned(ratingId, memberId));
  }

  private Rating findOwned(Long ratingId, Long memberId) {
    Rating rating =
        ratingRepository
            .findById(ratingId)
            .orElseThrow(() -> new ApiException(404, "RATING404", "존재하지 않는 평점입니다."));
    if (!rating.getMember().getMemberId().equals(memberId))
      throw new ApiException(403, "RATING403", "본인의 평점만 수정하거나 삭제할 수 있습니다.");
    return rating;
  }

  public List<RatingResponse> getRatingsByMember(Long memberId) {
    if (!memberRepository.existsById(memberId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");
    }
    return ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId).stream()
        .map(RatingResponse::from)
        .toList();
  }
}
