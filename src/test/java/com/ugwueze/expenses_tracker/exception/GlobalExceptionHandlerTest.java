package com.ugwueze.expenses_tracker.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("ResourceNotFoundException -> 404 and structured error response")
    void testNotFound() throws Exception {
        mvc.perform(get("/__test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("resource missing"))
                .andExpect(jsonPath("$.path").value("/__test/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("DuplicateResourceException -> 409 Conflict")
    void testDuplicate() throws Exception {
        mvc.perform(get("/__test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("duplicate key"))
                .andExpect(jsonPath("$.path").value("/__test/duplicate"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("DataIntegrityViolationException -> 409 and errors[] contains root message")
    void testDataIntegrity() throws Exception {
        mvc.perform(get("/__test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Database constraint violation"))
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.errors[0]", containsString("db constraint violated")))
                .andExpect(jsonPath("$.path").value("/__test/data-integrity"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException -> 400 with field error details")
    void testMethodArgumentNotValid() throws Exception {
        mvc.perform(post("/__test/validate-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.errors", hasItem(containsString("name"))))
                .andExpect(jsonPath("$.path").value("/__test/validate-body"));
    }

    @Test
    @DisplayName("ConstraintViolationException (param) -> 400 with parameter error details")
    void testConstraintViolation() throws Exception {
        mvc.perform(get("/__test/param-violation").param("q", "ab"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.errors", hasItem(containsString("q"))))
                .andExpect(jsonPath("$.path").value("/__test/param-violation"));
    }

    @Test
    @DisplayName("HttpMessageNotReadableException -> 400 malformed JSON message")
    void testHttpMessageNotReadable() throws Exception {
        String invalidJson = "{ \"name\": \"ok\"";
        mvc.perform(post("/__test/malformed-json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Malformed request body")))
                .andExpect(jsonPath("$.path").value("/__test/malformed-json"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("AccessDeniedException -> 403 Forbidden")
    void testAccessDenied() throws Exception {
        mvc.perform(get("/__test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/__test/access-denied"));
    }

    @Test
    @DisplayName("Generic Exception -> 500 Internal Server Error with simple message and errors[]")
    void testGenericException() throws Exception {
        mvc.perform(get("/__test/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.errors", hasItem("boom")))
                .andExpect(jsonPath("$.path").value("/__test/runtime"));
    }
}
