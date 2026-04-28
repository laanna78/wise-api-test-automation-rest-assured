package test.security;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseOfWiseTests;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-SEC-02: Profiladatok lekérése hiányzó tokennel")
public class TcWiseSec02Test extends BaseOfWiseTests {

    @Test
    @DisplayName("Hibaüzenet ellenőrzése hiányzó Authorization fejléc esetén")
    public void missingTokenResponseTest() {
        given()
                .contentType(ContentType.JSON)
        .when()
                .get("/v1/profiles")
        .then()
                .statusCode(401)
                .body("error", equalTo("missing_token"))
                .body("error_description", equalTo("Missing token"));
    }
}
