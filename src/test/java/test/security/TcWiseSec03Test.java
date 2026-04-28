package test.security;

import test.BaseOfWiseTests;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-SEC-03: Profiladatok lekérése érvénytelen tokennel")
public class TcWiseSec03Test extends BaseOfWiseTests {
    @Test
    public void invalidTokenResponseTest() {
        given()
                .header("Authorization", "Bearer " + ConfigReader.getProperty("invalid_token"))
                .contentType(ContentType.JSON)
        .when()
                .get("/v1/profiles")
        .then()
                .log().ifValidationFails()
                .statusCode(401)
                .body("error", equalTo("invalid_token"))
                .body("error_description", equalTo("Invalid token"));
    }
}
