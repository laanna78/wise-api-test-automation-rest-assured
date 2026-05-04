package test.errorHandling;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseErr03Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-ERR-03: Hibakezelés - Nem létező devizakód")
    @Description("Quote indítása érvénytelen cél devizakóddal (ABC). A várt eredmény HTTP 400 és a hibás mező megjelölése.")
    public void invalidCurrencyCodeTest() {

        Map<String, Object> invalidBody = new HashMap<>();
        invalidBody.put("sourceCurrency", "EUR");
        invalidBody.put("targetCurrency", "ABC");
        invalidBody.put("sourceAmount", 10);
        invalidBody.put("payOut", "BALANCE");

        Response response = Allure.step("Lépés 1: POST kérés küldése 'ABC' devizakóddal", () -> {
            logger.info("Quote kérése érvénytelen cél devizával (ABC)...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
                    .body(invalidBody)
            .when()
                    .post("/v3/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/quotes");
        });

        Allure.step("Lépés 2: HTTP 400 Bad Request státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 400, step);
        });

        Allure.step("Lépés 3: Hibaüzenet tartalmának ellenőrzése", () -> {
            attachJson(response, "Hiba válasz - Érvénytelen devizakód");

            response.then()
                    .assertThat()
                    .body("errors[0].code", equalTo("CurrencyCode"))
                    .body("errors[0].path", equalTo("targetCurrency"))
                    .body("errors[0].message", containsString("That wasn't a valid ISO-4217 currency code"));

            logger.info("A rendszer helyesen visszautasította a kérést a megfelelő hibaüzenettel.");
        });
    }
}
