package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseFin09Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-09: Külső utalás indítása - amerikai kedvezményezettnek",
            dependsOnMethods = {"test.finance.TcWiseFin01Test.createUsaRecipientTest", "test.finance.TcWiseFin08Test.calculateRateForExternalUsdTransferTest"})
    @Description("A korábban létrehozott amerikai kedvezményezettnek történő utalás indítása a generált ajánlat alapján.")
    public void initiateExternalUsdTransferTest() {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("targetAccount", Integer.parseInt(ConfigReader.getProperty("recipient_id_usa")));
        requestBody.put("quoteUuid", ConfigReader.getProperty("quote_id"));
        requestBody.put("customerTransactionId", UUID.randomUUID().toString());

        Response response = Allure.step("Lépés 1: POST kérés küldése a külső utalás elindításához", () -> {
            logger.info("Külső utalás indítása (USD)...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
                    .body(requestBody)
            .when()
                    .post("/v1/transfers");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Utalás adatainak validálása és tranzakció ID mentése", () -> {
            attachJson(response, "Indított utalás adatai");

            response.then()
                    .assertThat()
                    .body("id", notNullValue())
                    .body("status", equalTo("incoming_payment_waiting"));

            String transferId = response.jsonPath().getString("id");
            ConfigReader.setProperty("transfer_id_usa", transferId);

            String customerTransactionId = response.jsonPath().getString("customerTransactionId");
            ConfigReader.setProperty("customer_transaction_id", customerTransactionId);

            logger.info("Az utalás sikeresen elindítva. Transfer ID: {}, Status: {}",
                    transferId, response.jsonPath().getString("status"));
        });
    }
}
