package test.security;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-SEC-01: Profiladatok lekérése")
public class TcWiseSec01Test extends BaseOfWiseTests {

    @Test
    public void getProfileDataTest() {
        given()
                .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                .contentType(ContentType.JSON)
        .when()
                .get("/v1/profiles")
        .then()
                .statusCode(200)
                .body("[0].id", equalTo(Integer.parseInt(ConfigReader.getProperty("expected_profile_id"))))
                .body("[0].details.firstName", equalTo(ConfigReader.getProperty("expected_first_name")))
                .body("[0].details.lastName", equalTo(ConfigReader.getProperty("expected_last_name")));
    }
}
