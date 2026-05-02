package test.balance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-BAL-07: Nem létező számla egyenlegének lekérdezése")
public class TcWiseBal07Test extends BaseOfWiseTests {
    @Test
    @Description("Lekérdezés indítása egy érvényes formátumú, de nem létező balanceId-val.")
    public void nonExistingBalanceTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése egy nem létező balanceId-val", () -> {
            logger.info("Lekérdezés indítása nem létező egyenleg azonosítóval...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balances/" + ConfigReader.getProperty("balance_id_na"));
        });

        Allure.step("Lépés 2: HTTP 403 Forbidden státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 403, step);
        });

        Allure.step("Lépés 3: Hibaüzenet validálása", () -> {
            attachJson(response, "Hibaüzenet");

            response.then()
                    .body("code", equalTo("non.existing.balance"));

            logger.info("A hibaüzenet validálása sikeres.");
        });
    }
}
