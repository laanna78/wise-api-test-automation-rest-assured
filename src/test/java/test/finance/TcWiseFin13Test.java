package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;


public class TcWiseFin13Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-13: Bejövő utalás szimulációja")
    @Description("Bejövő utalás szimulációja a megadott profilhoz tartozó USD egyenlegre.")
    public void simulateIncomingTransferTest() {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("currency", "USD");
        requestBody.put("amount", 200);

        Response response = Allure.step("Lépés 1: POST kérés küldése a bejövő utalás szimulálásához", () -> {
            logger.info("Bejövő USD utalás szimulálása (200 USD)...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
                    .body(requestBody)
            .when()
                    .post("/v1/simulation/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/bank-transactions/import");
        });

        Allure.step("Lépés 2: HTTP 201 Created státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 201, step);
        });

        Allure.step("Lépés 3: Válasz naplózása", () -> {
            attachJson(response, "Szimulált bejövő tranzakció");
            logger.info("A bejövő utalás szimulációja sikeresen megtörtént.");
        });
    }
}
