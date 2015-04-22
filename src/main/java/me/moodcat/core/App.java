package me.moodcat.core;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Test.
 * <p>
 * Hello world!
 * </p>
 */
@Path("/home")
public class App {

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    public static final void test() {

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String doSomething() {
        return "Hello World!";
    }
}
