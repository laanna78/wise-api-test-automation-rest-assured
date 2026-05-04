package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseFin15Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-15: Belső átvezetés indítása - fedezethiány", dependsOnMethods = "test.finance.TcWiseFin14Test.calculateRateWithInsufficientFundsTest")
    @Description("Belső átvezetés indítása az aktuális egyenleget meghaladó összeggel a TC-WISE-FIN-14-ben létrehozott quote alapján.")
    public void initiateTransferWithInsufficientFundsTest() {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("quoteId", ConfigReader.getProperty("quote_id_insufficient"));

        Response response = Allure.step("Lépés 1: POST kérés küldése a fedezethiányos utaláshoz", () -> {
            logger.info("Belső átvezetés indítása a fedezetet meghaladó quote használatával...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .header("X-idempotence-uuid", UUID.randomUUID().toString())
                    .contentType(JSON)
                    .body(requestBody)
            .when()
                    .post("/v2/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balance-movements");
        });

        Allure.step("Lépés 2: HTTP 422 Unprocessable Entity státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 422, step);
        });

        Allure.step("Lépés 3: Hibaüzenet validálása", () -> {
            attachJson(response, "Fedezethiány miatti hiba válasz");

            response.then()
                    .assertThat()
                    .body("code", equalTo("quote.payment-option-disabled"));

            logger.info("A rendszer fedezethiány miatt elutasította az átvezetést: quote.payment-option-disabled");
        });
    }
}
