package com.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateAccountRequest {
    @NotBlank(message = "holderName must not be blank")
    private String holderName;

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
}
