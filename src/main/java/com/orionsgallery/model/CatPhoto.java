package com.orionsgallery.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CatPhoto(
        @JsonProperty("id") long id,
        @JsonProperty("title") String title,
        @JsonProperty("url") String url,
        @JsonProperty("description") String description
) {}
