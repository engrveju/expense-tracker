package com.ugwueze.expenses_tracker.exception;

import jakarta.validation.constraints.NotBlank;

public  class BodyDto {
    @NotBlank(message = "name is required")
    private String name;

    public BodyDto() {}

    public BodyDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}