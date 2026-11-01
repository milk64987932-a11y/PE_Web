package com.umc.movie;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.repository.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DatabaseConflictIntegrationTest {
  @Autowired MockMvc mvc;
  @MockitoSpyBean MemberRepository members;
  @MockitoSpyBean RatingRepository ratings;

  @ParameterizedTest
  @ValueSource(strings = {"email", "nickname", "rating"})
  void databaseConstraintStillRejectsWhenPrecheckMisses(String kind) throws Exception {
    // Existing seeded rows remain untouched; only bypass the service precheck.
    if (kind.equals("rating")) {
      long member =
          members.findAll().stream()
              .filter(m -> m.getEmail().equals("noco@umc.com"))
              .findFirst()
              .orElseThrow()
              .getMemberId();
      doReturn(false).when(ratings).existsByMember_MemberIdAndMovieId(member, 550L);
      mvc.perform(
              post("/members/{m}/ratings", member)
                  .contentType("application/json")
                  .content("{\"movieId\":550,\"score\":4}"))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("RATING4091"));
    } else {
      String email =
          kind.equals("email") ? "noco@umc.com" : java.util.UUID.randomUUID() + "@example.com";
      String nickname =
          kind.equals("nickname")
              ? "수현"
              : "db" + java.util.UUID.randomUUID().toString().substring(0, 8);
      doReturn(false).when(members).existsByEmail(email);
      doReturn(false).when(members).existsByNickname(nickname);
      String body =
          new ObjectMapper()
              .writeValueAsString(
                  java.util.Map.of("email", email, "nickname", nickname, "password", "Umc!1234"));
      mvc.perform(post("/members").contentType("application/json").content(body))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value(kind.equals("email") ? "MEMBER4091" : "MEMBER4092"))
          .andExpect(jsonPath("$.result").isEmpty());
    }
  }
}
