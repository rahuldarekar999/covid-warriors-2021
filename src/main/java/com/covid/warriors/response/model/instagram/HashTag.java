package com.covid.warriors.response.model.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HashTag {
    @JsonProperty("edge_hashtag_to_media")
    private EdgeHashtagToMedia edgeHashtagToMedia;

    public EdgeHashtagToMedia getEdgeHashtagToMedia() {
        return edgeHashtagToMedia;
    }

    public void setEdgeHashtagToMedia(EdgeHashtagToMedia edgeHashtagToMedia) {
        this.edgeHashtagToMedia = edgeHashtagToMedia;
    }
}
