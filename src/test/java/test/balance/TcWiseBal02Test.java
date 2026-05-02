package test.balance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-BAL-02: USD egyenleg lekérése")
public class TcWiseBal02Test extends BaseOfWiseTests {

    @Test
    @Description("Az USD számla egyenleg (balance) adatainak és aktuális összegének ellenőrzése az egyenleg ID alapján.")
    public void getUSDBalanceTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése az USD egyenleg végpontra", () -> {
            logger.info("USD egyenleg lekérése...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balances/" +
                            ConfigReader.getProperty("balance_id_usd"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Egyenleg adatok és USD pénznem validálása", () -> {
            attachJson(response, "Egyenleg adatok");

            response.then()
                    .body("id", equalTo(Integer.parseInt(ConfigReader.getProperty("balance_id_usd"))))
                    .body("currency", equalTo("USD"))
                    .body("amount.value", notNullValue())
                    .body("amount.currency", equalTo("USD"))
                    .body("reservedAmount.value", notNullValue())
                    .body("reservedAmount.currency", equalTo("USD"))
                    .body("cashAmount.value", notNullValue())
                    .body("cashAmount.currency", equalTo("USD"))
                    .body("totalWorth.value", notNullValue())
                    .body("totalWorth.currency", equalTo("USD"))
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

            logger.info("USD egyenleg adatok sikeresen validálva.");
        });
    }
}
