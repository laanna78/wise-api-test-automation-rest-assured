package test.balance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TcWiseBal03Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-BAL-03: AUD egyenleg lekérése")
    @Description("Az AUD számla egyenleg (balance) adatainak és aktuális összegének ellenőrzése az egyenleg ID alapján.")
    public void getAUDBalanceTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése az AUD egyenleg végpontra", () -> {
            logger.info("AUD egyenleg lekérése...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balances/" +
                            ConfigReader.getProperty("balance_id_aud"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Egyenleg adatok és AUD pénznem validálása", () -> {
            attachJson(response, "Egyenleg adatok");

            response.then()
                    .assertThat()
                    .body("id", equalTo(Integer.parseInt(ConfigReader.getProperty("balance_id_aud"))))
                    .body("currency", equalTo("AUD"))
                    .body("amount.value", notNullValue())
                    .body("amount.currency", equalTo("AUD"))
                    .body("reservedAmount.value", notNullValue())
                    .body("reservedAmount.currency", equalTo("AUD"))
                    .body("cashAmount.value", notNullValue())
                    .body("cashAmount.currency", equalTo("AUD"))
                    .body("totalWorth.value", notNullValue())
                    .body("totalWorth.currency", equalTo("AUD"))
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
            ConfigReader.setProperty("aud_balance_before", amountValue.setScale(2, RoundingMode.HALF_UP).toString());

            logger.info("AUD egyenleg adatok sikeresen validálva. Az AUD számla kiinduló egyenlege: {}", amountValue);
        });
    }
}
