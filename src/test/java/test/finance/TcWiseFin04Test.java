package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class TcWiseFin04Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-04: Árfolyamkalkuláció átvezetéshez")
    @Description("Az EUR -> GBP átvezetés előtti árfolyamkalkuláció elvégzése és az ajánlat (quote) azonosítójának mentése.")
    public void calculateRateAndCreateQuoteTest() {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sourceCurrency", "EUR");
        requestBody.put("targetCurrency", "GBP");
        requestBody.put("sourceAmount", 10);
        requestBody.put("payOut", "BALANCE");

        Response response = Allure.step("Lépés 1: POST kérés küldése az árfolyamkalkulációhoz és ajánlat létrehozásához", () -> {
            logger.info("Árfolyamkalkuláció indítása EUR -> GBP...");
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

            BigDecimal expectedAmount = new BigDecimal("10.00");
            BigDecimal actualAmount = response.jsonPath().getObject("sourceAmount", BigDecimal.class);

            response.then()
                    .assertThat()
                    .body("id", notNullValue())
                    .body("sourceCurrency", equalTo("EUR"))
                    .body("targetCurrency", equalTo("GBP"))
                    .body("payOut", equalTo("BALANCE"));

            assertThat("A forrás összeg nem megfelelő!", actualAmount.compareTo(expectedAmount) == 0);
            String quoteId = response.jsonPath().getString("id");
            ConfigReader.setProperty("quote_id", quoteId);

            logger.info("Az ajánlat sikeresen létrejött. Quote ID: {}", quoteId);
        });
    }
}
