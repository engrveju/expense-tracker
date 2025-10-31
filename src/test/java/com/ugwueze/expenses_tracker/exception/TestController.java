package com.ugwueze.expenses_tracker.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;


@RestController
@RequestMapping("/__test")
@Validated
public class TestController {


    @GetMapping("/not-found")
    public void notFound() {
        throw new ResourceNotFoundException("resource missing");
    }

    @GetMapping("/duplicate")
    public void duplicate() {
        throw new DuplicateResourceException("duplicate key");
    }

    @GetMapping("/data-integrity")
    public void dataIntegrity() {
        throw new DataIntegrityViolationException("db constraint violated");
    }

    @GetMapping("/access-denied")
    public void accessDenied() throws AccessDeniedException {
        throw new AccessDeniedException("no access");
    }

    @GetMapping("/runtime")
    public void runtime() {
        throw new RuntimeException("boom");
    }


    @PostMapping("/validate-body")
    public void validateBody(@RequestBody @Valid BodyDto body) {

    }


    @GetMapping("/param-violation")
    public void paramViolation(@RequestParam("q") @Size(min = 3) String q) {

    }

    @PostMapping("/malformed-json")
    public void malformedJson(@RequestBody BodyDto body) {

    }
}
