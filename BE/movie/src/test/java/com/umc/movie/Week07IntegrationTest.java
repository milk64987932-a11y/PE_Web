package com.umc.movie;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.umc.movie.config.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
class Week07IntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JwtProvider jwt;
  @Value("${jwt.secret}") String secret;

  JsonNode call(MockHttpServletRequestBuilder request, int status, String code) throws Exception {
    String body = mvc.perform(request).andExpect(status().is(status))
        .andExpect(jsonPath("$.code").value(code)).andReturn().getResponse().getContentAsString();
    return json.readTree(body).path("result");
  }

  MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder request, Object body) throws Exception {
    return request.contentType("application/json").content(json.writeValueAsBytes(body));
  }

  Map<String, String> account() throws Exception {
    String name = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    var account = Map.of("email", name + "@example.com", "nickname", name, "password", "Umc!1234");
    call(body(post("/members"), account), 201, "COMMON201");
    return account;
  }

  String login(Map<String, String> account) throws Exception {
    JsonNode result = call(body(post("/auth/login"), account), 200, "COMMON200");
    assertEquals(account.get("nickname"), result.path("nickname").asText());
    assertFalse(result.has("password"));
    return result.path("accessToken").asText();
  }

  MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
    return request.header("Authorization", "Bearer " + token);
  }

  @Test
  void requiredNineSteps() throws Exception {
    var a = account(); var b = account();
    String at = login(a), bt = login(b);
    long id = call(body(auth(post("/ratings"), at), Map.of("movieId",550,"score",5)),
        201,"COMMON201").path("ratingId").asLong();
    assertTrue(id > 0);
    call(body(auth(post("/ratings"),at), Map.of("movieId",550,"score",4)),409,"RATING409_1");
    call(body(auth(patch("/ratings/{id}",id),at),Map.of("score",3)),200,"COMMON200");
    call(body(auth(patch("/ratings/{id}",id),bt),Map.of("score",1)),403,"RATING403_1");
    call(auth(delete("/ratings/{id}",id),bt),403,"RATING403_1");
    call(body(patch("/ratings/{id}",id),Map.of("score",2)),401,"AUTH401_2");
    call(auth(delete("/ratings/{id}",Long.MAX_VALUE),at),404,"RATING404_1");
    assertTrue(call(auth(delete("/ratings/{id}",id),at),200,"COMMON200").isNull());
  }

  @Test
  void loginErrorsMatch() throws Exception {
    var a=account();
    String one=mvc.perform(body(post("/auth/login"),Map.of("email",a.get("email"),"password","wrong")))
        .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
    String two=mvc.perform(body(post("/auth/login"),Map.of("email","absent@example.com","password","wrong")))
        .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
    assertEquals(one,two);
    assertEquals("AUTH401_1",json.readTree(one).path("code").asText());
    assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.",json.readTree(one).path("message").asText());
  }

  @Test
  void profileAndValidation() throws Exception {
    var a=account(); var b=account(); String token=login(a);
    call(get("/members/me"),401,"AUTH401_2");
    JsonNode me=call(auth(get("/members/me"),token),200,"COMMON200");
    assertEquals(a.get("email"),me.path("email").asText());
    assertFalse(me.path("createdAt").asText().isBlank());
    assertFalse(me.has("password"));
    call(body(auth(patch("/members/me"),token),Map.of("nickname",a.get("nickname"))),200,"COMMON200");
    call(body(auth(patch("/members/me"),token),Map.of("nickname",b.get("nickname"))),409,"MEMBER409_1");
    JsonNode changed=call(body(auth(patch("/members/me"),token),Map.of("nickname","수정"+a.get("nickname"))),200,"COMMON200");
    assertEquals("수정"+a.get("nickname"),changed.path("nickname").asText());
    for(String name:List.of("x","x".repeat(13),"  "))
      call(body(auth(patch("/members/me"),token),Map.of("nickname",name)),400,"COMMON400");
  }

  @Test
  void rejectsInvalidExpiredAndDeletedMemberTokens() throws Exception {
    String token=login(account());
    String[] parts=token.split("\\.");
    String payload=Base64.getUrlEncoder().withoutPadding().encodeToString("{\"sub\":\"999\",\"exp\":9999999999}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    call(auth(get("/members/me"),parts[0]+"."+payload+"."+parts[2]),401,"AUTH401_3");
    var key=Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    String expired=Jwts.builder().subject("1").expiration(new Date(System.currentTimeMillis()-60000))
        .signWith(key,Jwts.SIG.HS256).compact();
    call(auth(get("/members/me"),expired),401,"AUTH401_4");
    String missingExpiry=Jwts.builder().subject("1").signWith(key,Jwts.SIG.HS256).compact();
    call(auth(get("/members/me"),missingExpiry),401,"AUTH401_3");
    for(String invalid:List.of("bad","Bearer ","Basic abc"))
      call(get("/members/me").header("Authorization",invalid),401,"AUTH401_3");
    String deleted=jwt.create(Long.MAX_VALUE);
    call(auth(get("/members/me"),deleted),404,"MEMBER404_1");
    call(body(auth(post("/ratings"),deleted),Map.of("movieId",550,"score",4)),404,"MEMBER404_1");
    call(body(auth(patch("/ratings/1"),deleted),Map.of("score",4)),404,"MEMBER404_1");
    call(auth(delete("/ratings/1"),deleted),404,"MEMBER404_1");
    call(get("/members/1/ratings/1"),401,"AUTH401_2");
    mvc.perform(options("/ratings").header("Origin","http://localhost:5173")
        .header("Access-Control-Request-Method","POST")
        .header("Access-Control-Request-Headers","Authorization,Content-Type"))
        .andExpect(status().isOk());
  }
}
