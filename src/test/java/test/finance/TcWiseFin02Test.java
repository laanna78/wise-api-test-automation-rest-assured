package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-FIN-02: Kedvezményezett létrehozása - európai számla")
public class TcWiseFin02Test extends BaseOfWiseTests {
    @Test
    @Description("Új külső bankszámla rögzítése a profilhoz 'iban' típussal, és a kapott ID elmentése.")
    public void createEurRecipientTest() {

        Map<String, Object> details = new HashMap<>();
        details.put("legalType", "PRIVATE");
        details.put("iban", "DE89370400440532013000");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("currency", "EUR");
        requestBody.put("type", "iban");
        requestBody.put("accountHolderName", "Német Kedvezményezett");
        requestBody.put("details", details);

        Response response = Allure.step("Lépés 1: POST kérés küldése az új európai kedvezményezett létrehozásához", () -> {
            logger.info("Európai (IBAN) kedvezményezett létrehozása...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
                    .body(requestBody)
            .when()
                    .post("/v1/accounts");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Válasz validálása és a Recipient ID mentése", () -> {
            attachJson(response, "Létrehozott kedvezményezett");

            response.then()
                    .body("id", notNullValue())
                    .body("accountHolderName", equalTo("Német Kedvezményezett"))
                    .body("currency", equalTo("EUR"))
                    .body("details.iban", equalTo("DE89370400440532013000"));

            String recipientIdEur = response.jsonPath().getString("id");
            ConfigReader.setProperty("recipient_id_eur", recipientIdEur);

            logger.info("Sikeres mentés. Kedvezményezett ID: {}", recipientIdEur);
        });
    }
}
