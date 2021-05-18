package com.covid.warriors.response.model.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
    @JsonProperty("hashtag")
    private HashTag hashTag;

    public HashTag getHashTag() {
        return hashTag;
    }

    public void setHashTag(HashTag hashTag) {
        this.hashTag = hashTag;
    }
}
