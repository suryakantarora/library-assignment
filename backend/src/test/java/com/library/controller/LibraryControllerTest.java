package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryControllerTest {

    private static final String TOKEN = "X-Auth-Token";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void protectedEndpointsRequireLogin() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Please login first."));
    }

    @Test
    void blankNameIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedBodyIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body."));
    }

    @Test
    void nonAdminCannotAddBooks() throws Exception {
        String token = login("dave");

        mvc.perform(post("/api/books").header(TOKEN, token).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Dune\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only admin can add books to the library."));
    }

    @Test
    void borrowFlowReturnsSpecMessages() throws Exception {
        String admin = login("admin");
        mvc.perform(post("/api/books").header(TOKEN, admin).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Dune\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Book \"Dune\" has been added to the library."));

        String erin = login("erin");
        mvc.perform(post("/api/borrow").header(TOKEN, erin).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Dune\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You borrowed \"Dune\"."));

        String frank = login("frank");
        mvc.perform(post("/api/borrow").header(TOKEN, frank).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Dune\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Sorry, \"Dune\" is currently not available."));
        mvc.perform(post("/api/waitlist").header(TOKEN, frank).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Dune\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You are added to the wait list of \"Dune\", your position is 1."));
        mvc.perform(get("/api/status").header(TOKEN, frank))
                .andExpect(jsonPath("$.waitlists[0].title").value("Dune"))
                .andExpect(jsonPath("$.waitlists[0].position").value(1));
    }

    @Test
    void logoutInvalidatesToken() throws Exception {
        String token = login("gina");

        mvc.perform(post("/api/auth/logout").header(TOKEN, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goodbye, gina!"));
        mvc.perform(get("/api/status").header(TOKEN, token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void detailOfUnknownBookReturnsNotFound() throws Exception {
        String token = login("henry");

        mvc.perform(get("/api/books/detail").param("title", "Nope").header(TOKEN, token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sorry, \"Nope\" is not registered."));
    }

    private String login(String name) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readValue(body, LoginResponse.class).token();
    }
}
