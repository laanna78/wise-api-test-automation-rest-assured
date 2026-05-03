package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-FIN-12: Idempotencia ellenőrzése")
public class TcWiseFin12Test extends BaseOfWiseTests {
    @Test
    @Description("Ugyanazon utalás elküldése kétszer ugyanazzal az egyedi UUID-val annak igazolására, hogy nem jön létre új tranzakció.")
    public void checkIdempotencyTest() {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("targetAccount", Integer.parseInt(ConfigReader.getProperty("recipient_id_usa")));
        requestBody.put("quoteUuid", ConfigReader.getProperty("quote_id"));
        requestBody.put("customerTransactionId", ConfigReader.getProperty("customer_transaction_id"));

        Response response = Allure.step("Lépés 1: POST kérés küldése ugyanazzal a customerTransactionId-val", () -> {
            logger.info("Ismételt utalás küldése az idempotencia ellenőrzéséhez...");
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

        Allure.step("Lépés 3: Annak ellenőrzése, hogy a rendszer a korábbi tranzakciót adta vissza", () -> {
            attachJson(response, "Idempotens válasz");

            String currentTransferId = response.jsonPath().getString("id");
            long previousTransferId = Long.parseLong(ConfigReader.getProperty("transfer_id_usa"));

            response.then()
                    .body("id", equalTo(previousTransferId))
                    .body("customerTransactionId", equalTo(ConfigReader.getProperty("customer_transaction_id")));

            logger.info("Az idempotencia validálva. Visszakapott ID: {}, megegyezik a korábbival: {}",
                    currentTransferId, previousTransferId);
        });
    }
}
