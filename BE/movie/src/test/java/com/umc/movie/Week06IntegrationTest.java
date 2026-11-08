package com.umc.movie;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(print = org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint.NONE)
@Transactional
class Week06IntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired PasswordEncoder encoder;
  @Autowired com.umc.movie.config.JwtProvider jwt;

  String suffix() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
  }

  Map<String, Object> signup() {
    String s = suffix();
    return new HashMap<>(
        Map.of("email", s + "@example.com", "nickname", s, "password", "Umc!1234"));
  }

  long member(Map<String, Object> body) throws Exception {
    String response =
        mvc.perform(
                post("/members")
                    .contentType("application/json")
                    .content(json.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.result.password").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json.readTree(response).path("result").path("memberId").asLong();
  }

  long rating(long member) throws Exception {
    return json.readTree(
            mvc.perform(
                    post("/ratings").header("Authorization", "Bearer " + jwt.create(member))
                        .contentType("application/json")
                        .content("{\"movieId\":550,\"score\":5}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString())
        .path("result").path("ratingId")
        .asLong();
  }

  @Test
  void bcryptAndGetCompatibility() throws Exception {
    var body = signup();
    long m = member(body);
    String hash =
        jdbc.queryForObject("select password from member where member_id=?", String.class, m);
    assertNotEquals(body.get("password"), hash);
    assertEquals(60, hash.length());
    assertTrue(encoder.matches("Umc!1234", hash));
    mvc.perform(get("/members/email/" + body.get("email")))
        .andExpect(jsonPath("$.available").value(false));
    mvc.perform(get("/members/nickname/" + body.get("nickname")))
        .andExpect(jsonPath("$.available").value(false));
  }

  @ParameterizedTest
  @ValueSource(strings = {"email", "nickname", "password", "longNickname", "longPassword"})
  void invalidSignup(String field) throws Exception {
    var body = signup();
    if (field.equals("longNickname")) body.put("nickname", "x".repeat(13));
    else if (field.equals("longPassword")) body.put("password", "Umc!1234" + "x".repeat(72));
    else body.put(field, field.equals("nickname") ? "x" : "bad");
    mvc.perform(
            post("/members").contentType("application/json").content(json.writeValueAsString(body)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.result").isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {"email", "nickname"})
  void duplicateMember(String field) throws Exception {
    var first = signup();
    member(first);
    var second = signup();
    second.put(field, first.get(field));
    mvc.perform(
            post("/members")
                .contentType("application/json")
                .content(json.writeValueAsString(second)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(field.equals("email") ? "MEMBER4091" : "MEMBER4092"));
  }

  @Test
  void crudAndOwnership() throws Exception {
    long m = member(signup()), other = member(signup()), r = rating(m);
    mvc.perform(
            post("/ratings").header("Authorization", "Bearer " + jwt.create(m))
                .contentType("application/json")
                .content("{\"movieId\":550,\"score\":4}"))
        .andExpect(status().isConflict());
    for (var method :
        List.of(
            patch("/ratings/{r}", r).header("Authorization", "Bearer " + jwt.create(other))
                .contentType("application/json")
                .content("{\"score\":3}"),
            delete("/ratings/{r}", r).header("Authorization", "Bearer " + jwt.create(other))))
      mvc.perform(method).andExpect(status().isForbidden());
    mvc.perform(
            patch("/ratings/{r}", r).header("Authorization", "Bearer " + jwt.create(m))
                .contentType("application/json")
                .content("{\"score\":3,\"comment\":\"updated\"}"))
        .andExpect(status().isOk());
    mvc.perform(get("/members/{m}/ratings", m))
        .andExpect(jsonPath("$[0].score").value(3))
        .andExpect(jsonPath("$[0].comment").value("updated"));
    assertEquals(
        3, jdbc.queryForObject("select score from rating where rating_id=?", Integer.class, r));
    mvc.perform(delete("/ratings/{r}", r).header("Authorization", "Bearer " + jwt.create(m)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("COMMON200"))
        .andExpect(jsonPath("$.result").isEmpty());
    mvc.perform(get("/members/{m}/ratings", m)).andExpect(content().json("[]"));
    assertEquals(
        0, jdbc.queryForObject("select count(*) from rating where rating_id=?", Integer.class, r));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "6", "1.5", "null", "\"five\""})
  void invalidScore(String score) throws Exception {
    long m = member(signup());
    mvc.perform(
            post("/ratings").header("Authorization", "Bearer " + jwt.create(m))
                .contentType("application/json")
                .content("{\"movieId\":550,\"score\":" + score + "}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            patch("/ratings/1").header("Authorization", "Bearer " + jwt.create(m))
                .contentType("application/json")
                .content("{\"score\":" + score + "}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void commentAndMovieValidation() throws Exception {
    long m = member(signup());
    for (Object movie : List.of(0, -1, 1.5)) {
      mvc.perform(
              post("/ratings").header("Authorization", "Bearer " + jwt.create(m))
                  .contentType("application/json")
                  .content(json.writeValueAsString(Map.of("movieId", movie, "score", 4))))
          .andExpect(status().isBadRequest());
    }
    mvc.perform(
            post("/ratings").header("Authorization", "Bearer " + jwt.create(m))
                .contentType("application/json")
                .content(
                    json.writeValueAsString(
                        Map.of("movieId", 550, "score", 4, "comment", "x".repeat(501)))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void missingResources() throws Exception {
    mvc.perform(
            post("/ratings").header("Authorization", "Bearer " + jwt.create(Long.MAX_VALUE))
                .contentType("application/json")
                .content("{\"movieId\":550,\"score\":5}"))
        .andExpect(status().isNotFound());
    mvc.perform(
            patch("/ratings/9223372036854775807").header("Authorization", "Bearer " + jwt.create(Long.MAX_VALUE))
                .contentType("application/json")
                .content("{\"score\":5}"))
        .andExpect(status().isNotFound());
    mvc.perform(delete("/ratings/9223372036854775807").header("Authorization", "Bearer " + jwt.create(Long.MAX_VALUE))).andExpect(status().isNotFound());
  }
}
