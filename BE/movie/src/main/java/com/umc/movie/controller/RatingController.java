package com.umc.movie.controller;

import com.umc.movie.dto.*;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController

@RequiredArgsConstructor
@Validated
public class RatingController {
  private final RatingService ratingService;

  @PostMapping("/ratings")
  public ResponseEntity<ApiResponse<RatingResponse>> create(
      @RequestAttribute Long memberId, @Valid @RequestBody CreateRatingRequest request) {
    return ResponseEntity.status(201)
        .body(ApiResponse.created("평점 등록에 성공했습니다.", ratingService.create(memberId, request)));
  }

  @PatchMapping("/ratings/{ratingId}")
  public ApiResponse<Void> update(
      @RequestAttribute Long memberId,
      @PathVariable @Positive Long ratingId,
      @Valid @RequestBody UpdateRatingRequest request) {
    ratingService.update(ratingId, memberId, request);
    return ApiResponse.ok("평점 수정에 성공했습니다.", null);
  }

  @DeleteMapping("/ratings/{ratingId}")
  public ApiResponse<Void> delete(
      @RequestAttribute Long memberId, @PathVariable @Positive Long ratingId) {
    ratingService.delete(ratingId, memberId);
    return ApiResponse.ok("평점 삭제에 성공했습니다.", null);
  }

  @GetMapping("/members/{memberId}/ratings")
  public List<RatingResponse> getRatings(@PathVariable @Positive Long memberId) {
    return ratingService.getRatingsByMember(memberId);
  }
}
