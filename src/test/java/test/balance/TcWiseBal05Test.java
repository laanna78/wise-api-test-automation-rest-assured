package test.balance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TcWiseBal05Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-BAL-05: Adatstruktúra validáció")
    @Description("A válaszban érkező JSON objektum szerkezetének, adattípusainak és alapértelmezett értékeinek ellenőrzése.")
    public void validateDataStructureTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése a GBP egyenleg végpontra", () -> {
            logger.info("GBP egyenleg lekérése struktúra validációhoz...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balances/" + ConfigReader.getProperty("balance_id_gbp"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Adatstruktúra, típusok és üzleti szabályok validálása", () -> {
            attachJson(response, "Validálandó JSON válasz");

            response.then()
                    .assertThat()
                    .body("id", instanceOf(Integer.class))
                    .body("currency", allOf(
                            instanceOf(String.class),
                            hasLength(3),
                            matchesPattern("^[A-Z]{3}$")
                    ))
                    .body("amount.value", validAmount())
                    .body("amount.currency", equalTo(response.path("currency")))
                    .body("reservedAmount.value", validAmount())
                    .body("reservedAmount.currency", equalTo(response.path("currency")))
                    .body("cashAmount.value", validAmount())
                    .body("cashAmount.currency", equalTo(response.path("currency")))
                    .body("totalWorth.value", validAmount())
                    .body("totalWorth.currency", equalTo(response.path("currency")))
                    .body("cashAmount.value", (response.path("cashAmount.value") != null && response.path("totalWorth.value") != null) ?
                            equalTo(response.path("totalWorth.value")) : notNullValue())
                    .body("type", equalTo("STANDARD"))
                    .body("investmentState", equalTo("NOT_INVESTED"))
                    .body("creationTime", matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$"))
                    .body("modificationTime", matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$"))
                    .body("visible", instanceOf(Boolean.class))
                    .body("primary", instanceOf(Boolean.class))
                    .body("recipientId", instanceOf(Integer.class));

            logger.info("Adatstruktúra validáció sikeresen befejeződött.");
        });
    }
}
