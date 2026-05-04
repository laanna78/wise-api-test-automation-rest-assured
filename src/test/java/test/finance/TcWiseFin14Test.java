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


public class TcWiseFin14Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-14: Árfolyamkalkuláció fedezethiányos belső átvezetéshez")
    @Description("Az EUR -> USD belső átvezetés előtti árfolyamkalkuláció elvégzése és ajánlat létrehozása, fedezethiánnyal (extrém magas összeg).")
    public void calculateRateWithInsufficientFundsTest() {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sourceCurrency", "EUR");
        requestBody.put("targetCurrency", "USD");
        requestBody.put("sourceAmount", 10000000);
        requestBody.put("payOut", "BALANCE");

        Response response = Allure.step("Lépés 1: POST kérés küldése az árfolyamkalkulációhoz (fedezethiányos összeggel)", () -> {
            logger.info("Árfolyamkalkuláció indítása fedezethiányos EUR -> USD belső átvezetéshez...");
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
            attachJson(response, "Létrehozott fedezethiányos ajánlat (Quote)");

            BigDecimal expectedAmount = new BigDecimal("10000000.00");
            BigDecimal actualAmount = response.jsonPath().getObject("sourceAmount", BigDecimal.class);

            response.then()
                    .assertThat()
                    .body("id", notNullValue())
                    .body("sourceCurrency", equalTo("EUR"))
                    .body("targetCurrency", equalTo("USD"))
                    .body("payOut", equalTo("BALANCE"));

            assertThat("A forrás összeg nem megfelelő!", actualAmount.compareTo(expectedAmount) == 0);
            String quoteId = response.jsonPath().getString("id");
            ConfigReader.setProperty("quote_id_insufficient", quoteId);

            logger.info("Az ajánlat sikeresen létrejött a fedezethiány ellenére is. Quote ID: {}", quoteId);
        });
    }
}
