package test.security;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
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
                    .contentType(JSON)
            .when()
                    .get("/v1/profiles");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Profil ID és névadatok validálása a válaszban", () -> {
            attachJson(response, "Validált JSON válasz");
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
