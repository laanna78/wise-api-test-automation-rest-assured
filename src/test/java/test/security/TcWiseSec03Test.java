package test.security;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-SEC-03: Profiladatok lekérése érvénytelen tokennel")
public class TcWiseSec03Test extends BaseOfWiseTests {
    @Test
    @Description("Annak ellenőrzése, hogy az API elutasítja-e a kérést (401), ha a megadott token lejárt vagy érvénytelen.")
    public void invalidTokenResponseTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése érvénytelen tokennel", () -> {
            String invalidToken = ConfigReader.getProperty("invalid_token");
            logger.info("Kérés indítása érvénytelen tokennel: {}", invalidToken);

            return given()
                    .header("Authorization", "Bearer " + invalidToken)
                    .contentType(JSON)
            .when()
                    .get("/v1/profiles");
        });

        Allure.step("Lépés 2: HTTP 401 Unauthorized státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 401, step);
        });

        Allure.step("Lépés 3: Specifikus hibaüzenet validálása (invalid_token)", () -> {
            attachJson(response, "Érvénytelen token hibaüzenet");

            response.then()
                    .body("error", equalTo("invalid_token"))
                    .body("error_description", equalTo("Invalid token"));

            logger.info("A hibaüzenet validálása sikeres.");
        });
    }
}
