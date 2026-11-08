package com.umc.movie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(print = org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint.NONE)
@Transactional
class MovieApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @Test
    void nicknameAvailability() throws Exception {
        mvc.perform(get("/members/nickname/수현"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/members/nickname/new-" + UUID.randomUUID()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void emailAvailability() throws Exception {
        mvc.perform(get("/members/email/noco@umc.com"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/members/email/" + UUID.randomUUID() + "@example.com"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void ratingsAreDescendingAndContainOnlyDtoFields() throws Exception {
        Long memberId = jdbc.queryForObject(
                "SELECT member_id FROM member WHERE email = 'noco@umc.com'", Long.class);
        Long latestId = jdbc.queryForObject(
                "SELECT MAX(rating_id) FROM rating WHERE member_id = ?", Long.class, memberId);
        mvc.perform(get("/members/{memberId}/ratings", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ratingId").value(latestId))
                .andExpect(jsonPath("$[0].movieId").value(155))
                .andExpect(jsonPath("$[1].movieId").value(550))
                .andExpect(jsonPath("$[0].createdAt", not(emptyOrNullString())))
                .andExpect(jsonPath("$[0].updatedAt", not(emptyOrNullString())))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].member").doesNotExist());
    }

    @Test
    void missingMemberReturns404() throws Exception {
        mvc.perform(get("/members/9223372036854775807/ratings"))
                .andExpect(status().isNotFound());
    }

    @Test
    void existingMemberWithoutRatingsReturnsEmptyArray() throws Exception {
        String suffix = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO member(email,password,nickname,created_at,updated_at) VALUES(?,?,?,NOW(),NOW())",
                suffix + "@example.com", "TEST_ONLY_NOT_A_PASSWORD", suffix);
        Long memberId = jdbc.queryForObject(
                "SELECT member_id FROM member WHERE email = ?", Long.class, suffix + "@example.com");
        mvc.perform(get("/members/{memberId}/ratings", memberId))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsInvalidParameters() throws Exception {
        mvc.perform(get("/members/email/not-an-email")).andExpect(status().isBadRequest());
        mvc.perform(get("/members/0/ratings")).andExpect(status().isBadRequest());
    }

    @Test
    void corsAllowsFrontendOriginAndPreflight() throws Exception {
        mvc.perform(get("/members/nickname/수현").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/members/1/ratings")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("PATCH")));
    }

    @Test
    void corsRejectsUnapprovedOrigin() throws Exception {
        mvc.perform(get("/members/nickname/수현").header("Origin", "http://unapproved.example"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
