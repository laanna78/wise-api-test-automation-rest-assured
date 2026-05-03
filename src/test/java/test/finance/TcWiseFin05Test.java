package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-FIN-05: Saját számlák közötti átvezetés: EUR -> GBP")
public class TcWiseFin05Test extends BaseOfWiseTests {
    @Test
    @Description("Pénz mozgatása EUR és GBP egyenleg között belső váltással, és a tranzakció azonosító mentése.")
    public void internalTransferEurToGbpTest() {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("quoteId", ConfigReader.getProperty("quote_id"));

        Response response = Allure.step("Lépés 1: POST kérés küldése az átvezetéshez", () -> {
            logger.info("Átvezetés indítása saját számlák között...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .header("X-idempotence-uuid", UUID.randomUUID().toString())
                    .contentType(JSON)
                    .body(requestBody)
            .when()
                    .post("/v2/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balance-movements");
        });

        Allure.step("Lépés 2: HTTP 201 Created státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 201, step);
        });

        Allure.step("Lépés 3: Tranzakció validálása, majd az ID és az összegek mentése", () -> {
            attachJson(response, "Sikeres átvezetés adatai");

            response.then()
                    .body("id", notNullValue())
                    .body("state", anyOf(equalTo("COMPLETED"), equalTo("PROCESSING")));

            String transactionId = response.jsonPath().getString("id");
            ConfigReader.setProperty("transaction_id", transactionId);

            BigDecimal sourceAmountValue = response.jsonPath().getObject("sourceAmount.value", BigDecimal.class);
            ConfigReader.setProperty("source_amount_value", sourceAmountValue.setScale(2, RoundingMode.HALF_UP).toString());

            BigDecimal targetAmountValue = response.jsonPath().getObject("targetAmount.value", BigDecimal.class);
            ConfigReader.setProperty("target_amount_value", targetAmountValue.setScale(2, RoundingMode.HALF_UP).toString());

            BigDecimal feeAmountsValue = response.jsonPath().getObject("feeAmounts[0].value", BigDecimal.class);
            ConfigReader.setProperty("fee_amounts_value", feeAmountsValue.setScale(2, RoundingMode.HALF_UP).toString());

            logger.info("Az átvezetés sikeres. Transaction ID: {}, source amount: {}, target amount: {}, " +
                    "fee amounts: {}",  transactionId, sourceAmountValue, targetAmountValue, feeAmountsValue);
        });
    }
}
