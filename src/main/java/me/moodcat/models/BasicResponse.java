package me.moodcat.models;

import lombok.Data;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class BasicResponse {

    private String test;

    private Integer number;

    private boolean aBoolean;

    /*
     * We might want to use @Data lombok annotations
     * to automatically generate and maintain getters/setters
     */
}
