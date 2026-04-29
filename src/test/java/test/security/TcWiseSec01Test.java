package test.security;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-SEC-01: Profiladatok lekérése")
public class TcWiseSec01Test extends BaseOfWiseTests {

    @Test
    @Description("Alapvető profiladatok (név, típus) lekérése érvényes tokennel és a válasz validálása.")
    public void getProfileDataTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése a /v1/profiles végpontra érvényes tokennel", () -> {
            logger.info("Kérés indítása a profil adatokért...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(ContentType.JSON)
            .when()
                    .get("/v1/profiles");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            logger.info("Státuszkód ellenőrzése...");
            int code = response.getStatusCode();
            step.parameter("Várt státuszkód", "200");
            step.parameter("Kapott státuszkód", code);
            response.then().statusCode(200);
        });

        Allure.step("Lépés 3: Profil ID és névadatok validálása a válaszban", () -> {
            String body = response.asPrettyString();
            Allure.addAttachment("Validált JSON válasz", "application/json", body);
            int expectedId = Integer.parseInt(ConfigReader.getProperty("expected_profile_id"));
            String expectedFirstName = ConfigReader.getProperty("expected_first_name");
            String expectedLastName = ConfigReader.getProperty("expected_last_name");

            logger.info("Várt adatok ellenőrzése - ID: {}, Név: {} {}", expectedId, expectedFirstName, expectedLastName);

            response.then()
                    .body("[0].id", equalTo(expectedId))
                    .body("[0].details.firstName", equalTo(expectedFirstName))
                    .body("[0].details.lastName", equalTo(expectedLastName));

            logger.info("Adatvalidálás sikeresen befejeződött.");
        });
    }
}
