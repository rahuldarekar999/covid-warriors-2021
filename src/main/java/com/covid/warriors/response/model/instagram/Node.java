package com.covid.warriors.response.model.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {

    @JsonProperty("accessibility_caption")
    private String accessibilityCaption;

    public String getAccessibilityCaption() {
        return accessibilityCaption;
    }

    public void setAccessibilityCaption(String accessibilityCaption) {
        this.accessibilityCaption = accessibilityCaption;
    }
}
