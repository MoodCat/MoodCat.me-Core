package me.moodcat.models;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class BasicResponse {

    private String test;

    private Integer number;

    private boolean aBoolean;

    /*
     * We might want to use @Data lombok annotations
     *  to automatically generate and maintain getters/setters
     */

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    @Override
    public String toString() {
        return "BasicResponse{" +
                "test='" + test + '\'' +
                ", number=" + number +
                ", aBoolean=" + aBoolean +
                '}';
    }
}
