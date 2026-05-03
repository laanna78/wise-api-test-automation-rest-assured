package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-FIN-08: Árfolyamkalkuláció külső utaláshoz")
public class TcWiseFin08Test extends BaseOfWiseTests {
    @Test
    @Description("Az USD -> külső USD átvezetés előtti árfolyamkalkuláció elvégzése és ajánlat létrehozása.")
    public void calculateRateForExternalUsdTransferTest() {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sourceCurrency", "USD");
        requestBody.put("targetCurrency", "USD");
        requestBody.put("sourceAmount", 100);
        requestBody.put("targetAmount", null);
        requestBody.put("targetAccount", Integer.parseInt(ConfigReader.getProperty("recipient_id_usa")));
        requestBody.put("payOut", null);

        Response response = Allure.step("Lépés 1: POST kérés küldése az árfolyamkalkulációhoz", () -> {
            logger.info("Külső USD -> USD árfolyamkalkuláció indítása...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
                    .body(requestBody)
            .when()
                    .post("/v3/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/quotes");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Ajánlat adatainak validálása és a quoteId mentése", () -> {
            attachJson(response, "Létrehozott ajánlat (Quote)");

            BigDecimal expectedAmount = new BigDecimal("100.00");
            BigDecimal actualAmount = response.jsonPath().getObject("sourceAmount", BigDecimal.class);

            response.then()
                    .body("id", notNullValue())
                    .body("sourceCurrency", equalTo("USD"))
                    .body("targetCurrency", equalTo("USD"))
                    .body("providedAmountType", equalTo("SOURCE"))
                    .body("payOut", equalTo("BANK_TRANSFER"))
                    .body("targetAccount", equalTo(Integer.parseInt(ConfigReader.getProperty("recipient_id_usa"))))
                    .body("status", equalTo("PENDING"));

            assertThat("A forrás összeg nem megfelelő!", actualAmount.compareTo(expectedAmount) == 0);
            String quoteId = response.jsonPath().getString("id");
            ConfigReader.setProperty("quote_id", quoteId);

            logger.info("Az ajánlat sikeresen létrejött. Quote ID: {}", quoteId);
        });
    }
}
