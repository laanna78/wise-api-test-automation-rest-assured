package test.balance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-BAL-04: GBP egyenleg lekérése")
public class TcWiseBal04Test extends BaseOfWiseTests {

    @Test
    @Description("A GBP számla egyenleg (balance) adatainak és aktuális összegének ellenőrzése az egyenleg ID alapján.")
    public void getGBPBalanceTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése a GBP egyenleg végpontra", () -> {
            logger.info("GBP egyenleg lekérése...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balances/" +
                            ConfigReader.getProperty("balance_id_gbp"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Egyenleg adatok és GBP pénznem validálása", () -> {
            attachJson(response, "Egyenleg adatok");

            response.then()
                    .body("id", equalTo(Integer.parseInt(ConfigReader.getProperty("balance_id_gbp"))))
                    .body("currency", equalTo("GBP"))
                    .body("amount.value", notNullValue())
                    .body("amount.currency", equalTo("GBP"))
                    .body("reservedAmount.value", notNullValue())
                    .body("reservedAmount.currency", equalTo("GBP"))
                    .body("cashAmount.value", notNullValue())
                    .body("cashAmount.currency", equalTo("GBP"))
                    .body("totalWorth.value", notNullValue())
                    .body("totalWorth.currency", equalTo("GBP"))
                    .body("type", notNullValue())
                    .body("name", nullValue())
                    .body("icon", nullValue())
                    .body("investmentState", notNullValue())
                    .body("creationTime", notNullValue())
                    .body("modificationTime", notNullValue())
                    .body("visible", equalTo(true))
                    .body("primary", equalTo(true))
                    .body("groupId", nullValue())
                    .body("recipientId", notNullValue());

            logger.info("GBP egyenleg adatok sikeresen validálva.");
        });
    }
}
