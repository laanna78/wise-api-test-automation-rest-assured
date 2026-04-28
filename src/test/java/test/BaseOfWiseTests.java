package test;

import utils.ConfigReader;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class BaseOfWiseTests {
    @BeforeAll
    public static void setup() {
        // Központi URL beállítás a ConfigReader segítségével
        RestAssured.baseURI = ConfigReader.getProperty("base_url");

    }
}
