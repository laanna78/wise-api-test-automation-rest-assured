package test.foreignExchange;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-FX-01: Forrásösszeg alapú Quote validáció")
public class TcWiseFx01Test extends BaseOfWiseTests {
    @Test
    @Description("Quote válasz matematikai, időrendi és logikai integritásának ellenőrzése.")
    public void sourceAmountBasedQuoteValidationTest() {

        Response response = Allure.step("Lépés 1: POST kérés küldése fix sourceAmount-tal", () -> given()
                .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                .contentType(JSON)
                .body(Map.of(
                        "sourceCurrency", "GBP",
                        "targetCurrency", "EUR",
                        "sourceAmount", 100,
                        "targetAccount", Integer.parseInt(ConfigReader.getProperty("recipient_id_eur"))
                ))
                .post("/v3/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/quotes"));

        Allure.step("Lépés 2: HTTP 200 OK ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Üzleti logika és matematikai validáció", () -> {
            attachJson(response, "Forrásösszeg alapú Quote válasz");

            List<Map<String, Object>> options = response.jsonPath().getList("paymentOptions");
            boolean hasEnabledOption = false;

            for (Map<String, Object> option : options) {
                // Díjak összege
                @SuppressWarnings("unchecked")
                Map<String, Object> feeMap = (Map<String, Object>) option.get("fee");
                BigDecimal transferwiseFee = new BigDecimal(feeMap.get("transferwise").toString());
                BigDecimal payInFee = new BigDecimal(feeMap.get("payIn").toString());
                BigDecimal totalFee = new BigDecimal(feeMap.get("total").toString());

                logger.info("A totalFee összege ({}) megfelel-e a transferwise fee ({}) és a payIn fee ({}) összegének.", totalFee, transferwiseFee, payInFee);
                Assertions.assertEquals(0, totalFee.compareTo(transferwiseFee.add(payInFee)), "Díjösszeg hiba!");

                // Célösszeg számítás: (Source - Fee) * Rate = Target
                BigDecimal rate = new BigDecimal(response.jsonPath().get("rate").toString());
                BigDecimal source = new BigDecimal(option.get("sourceAmount").toString());
                BigDecimal target = new BigDecimal(option.get("targetAmount").toString());

                BigDecimal calculatedTarget = source.subtract(totalFee).multiply(rate).setScale(2, RoundingMode.HALF_UP);
                logger.info("A céldeviza összege ({}) megfelel-e az elvártnak ({}).", calculatedTarget, target);
                Assertions.assertEquals(0, target.compareTo(calculatedTarget), "Célösszeg számítási hiba!");

                // Díj százalék validálása
                BigDecimal feePercentage = new BigDecimal(option.get("feePercentage").toString());
                BigDecimal calculatedPercentage = totalFee.divide(source, 4, RoundingMode.HALF_UP);
                logger.info("Az aktuális díj ({}) megfelel-e az elvárt díjnak ({}).", feePercentage, calculatedPercentage);
                Assertions.assertEquals(0, feePercentage.compareTo(calculatedPercentage), "Díj százalék hiba!");

                // Legalább egy opció aktív
                if (!(boolean) option.get("disabled")) hasEnabledOption = true;
                logger.info("Ez az opció aktív-e? {}", hasEnabledOption);
            }
            Assertions.assertTrue(hasEnabledOption, "Nincs egyetlen választható fizetési mód sem!");

            // Időrendi validáció
            LocalDateTime createdTime = parseToLocalDateTime(response.jsonPath().getString("createdTime"));
            LocalDateTime rateExpirationTime = parseToLocalDateTime(response.jsonPath().getString("rateExpirationTime"));
            LocalDateTime quoteExpirationTime = parseToLocalDateTime(response.jsonPath().getString("expirationTime"));

            logger.info("Az árfolyam lejárati ideje ({}) a létrehozás ideje ({}) utáni időpont-e.", rateExpirationTime, createdTime);
            logger.info("Az ajánlat lejárati ideje ({}) a létrehozás ideje ({}) utáni időpont-e.", quoteExpirationTime, createdTime);
            Assertions.assertTrue(rateExpirationTime.isAfter(createdTime), "Az árfolyam lejárata nem a jövőben van!");
            Assertions.assertTrue(quoteExpirationTime.isAfter(createdTime), "A quote lejárata nem a jövőben van!");

            // Metaadatok és Logikai kapcsolók
            response.then()
                    .body("profile", equalTo(Integer.parseInt(ConfigReader.getProperty("expected_profile_id"))))
                    .body("sourceCurrency", equalTo("GBP"))
                    .body("targetCurrency", equalTo("EUR"))
                    .body("status", equalTo("PENDING"))
                    .body("type", equalTo("REGULAR"))
                    .body("id", notNullValue())
                    .body("providedAmountType", equalTo("SOURCE"))
                    .body("rateType", equalTo("FIXED"))
                    .body("guaranteedTargetAmount", equalTo(false));
        });
    }
}
