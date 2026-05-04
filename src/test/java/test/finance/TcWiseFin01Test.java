package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseFin01Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-01: Kedvezményezett létrehozása - amerikai számla")
    @Description("Új külső bankszámla rögzítése a profilhoz 'aba' típussal, és a kapott ID elmentése.")
    public void createUsaRecipientTest() {

        Map<String, Object> details = new HashMap<>();
        details.put("legalType", "PRIVATE");
        details.put("abartn", "325171232");
        details.put("accountNumber", "987654322");
        details.put("accountType", "CHECKING");
        details.put("address", new HashMap<String, String>() {{
            put("state", "NY");
            put("city", "New York");
            put("country", "US");
            put("postCode", "10001");
            put("firstLine", "123 Test Street");
        }});

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("currency", "USD");
        requestBody.put("type", "aba");
        requestBody.put("accountHolderName", "Amerikai Kedvezményezett");
        requestBody.put("details", details);

        Response response = Allure.step("Lépés 1: POST kérés küldése az új amerikai kedvezményezett létrehozásához", () -> {
            logger.info("Amerikai kedvezményezett létrehozása...");
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
                    .assertThat()
                    .body("id", notNullValue())
                    .body("accountHolderName", equalTo("Amerikai Kedvezményezett"))
                    .body("currency", equalTo("USD"));

            String recipientIdUsa = response.jsonPath().getString("id");
            ConfigReader.setProperty("recipient_id_usa", recipientIdUsa);

            logger.info("Sikeres mentés. Kedvezményezett ID: {}", recipientIdUsa);
        });
    }
}
