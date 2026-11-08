package com.umc.movie.dto;

import jakarta.validation.constraints.*;

public record UpdateMemberRequest(@NotBlank @Size(min = 2, max = 12) String nickname) {}
