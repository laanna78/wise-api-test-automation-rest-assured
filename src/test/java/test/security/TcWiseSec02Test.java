package test.security;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseOfWiseTests;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-SEC-02: Profiladatok lekérése hiányzó tokennel")
public class TcWiseSec02Test extends BaseOfWiseTests {

    @Test
    @Description("Annak ellenőrzése, hogy az API elutasítja-e a kérést (401), ha hiányzik az Authorization fejléc.")
    public void missingTokenResponseTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése Authorization fejléc nélkül", () -> {
            logger.info("Kérés indítása token nélkül a /v1/profiles végpontra...");
            return given()
                    .contentType(ContentType.JSON)
            .when()
                    .get("/v1/profiles");
        });

        Allure.step("Lépés 2: HTTP 401 Unauthorized státuszkód ellenőrzése", (step) -> {
            logger.info("Státuszkód ellenőrzése...");
            int code = response.getStatusCode();
            step.parameter("Várt státuszkód", "401");
            step.parameter("Kapott státuszkód", String.valueOf(code));
            response.then().statusCode(401);
        });

        Allure.step("Lépés 3: Hibaüzenetek validálása a válaszban", () -> {
            logger.info("Hibaüzenet tartalmának ellenőrzése...");

            Allure.addAttachment("Hiba válasz body", "application/json", response.asPrettyString());

            response.then()
                    .body("error", equalTo("missing_token"))
                    .body("error_description", equalTo("Missing token"));

            logger.info("A hibaüzenet validálása sikeres.");
        });
    }
}
