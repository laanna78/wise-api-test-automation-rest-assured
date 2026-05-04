package test.balance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TcWiseBal01Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-BAL-01: EUR egyenleg lekérése")
    @Description("Az EUR számla egyenleg (balance) adatainak és aktuális összegének ellenőrzése az egyenleg ID alapján.")
    public void getEurBalanceTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése az EUR egyenleg végpontra", () -> {
            logger.info("EUR egyenleg lekérése...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balances/" + ConfigReader.getProperty("balance_id_eur"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Egyenleg adatok és EUR pénznem validálása", () -> {
            attachJson(response, "Egyenleg adatok");

            response.then()
                    .assertThat()
                    .body("id", equalTo(Integer.parseInt(ConfigReader.getProperty("balance_id_eur"))))
                    .body("currency", equalTo("EUR"))
                    .body("amount.value", notNullValue())
                    .body("amount.currency", equalTo("EUR"))
                    .body("reservedAmount.value", notNullValue())
                    .body("reservedAmount.currency", equalTo("EUR"))
                    .body("cashAmount.value", notNullValue())
                    .body("cashAmount.currency", equalTo("EUR"))
                    .body("totalWorth.value", notNullValue())
                    .body("totalWorth.currency", equalTo("EUR"))
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

            BigDecimal amountValue = response.jsonPath().getObject("amount.value", BigDecimal.class);
            ConfigReader.setProperty("eur_balance_before", amountValue.setScale(2, RoundingMode.HALF_UP).toString());

            logger.info("EUR egyenleg adatok sikeresen validálva. Az EUR számla kiinduló egyenlege: {}", amountValue);
        });
    }
}
