package test.foreignExchange;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TcWiseFx02Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FX-02: Célösszeg alapú Quote validáció", dependsOnMethods = "test.finance.TcWiseFin02Test.createEurRecipientTest")
    @Description("Quote validálása, ahol a célösszeg fix.")
    public void targetAmountBasedQuoteValidationTest() {

        Response response = Allure.step("Lépés 1: POST kérés küldése fix targetAmount-tal", () -> given()
                .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                .contentType(JSON)
                .body(Map.of(
                        "sourceCurrency", "GBP",
                        "targetCurrency", "EUR",
                        "targetAmount", 100,
                        "targetAccount", Integer.parseInt(ConfigReader.getProperty("recipient_id_eur"))
                ))
                .post("/v3/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/quotes"));

        Allure.step("Lépés 2: HTTP 200 OK ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Üzleti logika és matematikai validáció", () -> {
            attachJson(response, "Célösszeg alapú Quote válasz");

            BigDecimal rate = new BigDecimal(response.jsonPath().get("rate").toString());
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
                Assert.assertEquals(totalFee.compareTo(transferwiseFee.add(payInFee)), 0, "Díjösszeg hiba!");

                // Forrásösszeg számítás: (Target / Rate) + Fee = Source
                BigDecimal target = new BigDecimal(option.get("targetAmount").toString());
                BigDecimal source = new BigDecimal(option.get("sourceAmount").toString());

                BigDecimal calculatedSource = target.divide(rate, 10, RoundingMode.HALF_UP)
                        .add(totalFee)
                        .setScale(2, RoundingMode.HALF_UP);

                logger.info("A forrásdeviza összege ({}) megfelel-e az elvártnak ({}).", calculatedSource, source);
                Assert.assertEquals(source.compareTo(calculatedSource), 0,
                        String.format("Forrásösszeg hiba! Számított: %s, aktuális: %s", calculatedSource, source));

                // Legalább egy opció aktív
                if (!(boolean) option.get("disabled")) hasEnabledOption = true;
                logger.info("Ez az opció aktív-e? {}", hasEnabledOption);
            }
            Assert.assertTrue(hasEnabledOption, "Nincs egyetlen választható fizetési mód sem!");

            // Időrendi validáció
            LocalDateTime createdTime = parseToLocalDateTime(response.jsonPath().getString("createdTime"));
            LocalDateTime rateExpirationTime = parseToLocalDateTime(response.jsonPath().getString("rateExpirationTime"));

            logger.info("Az árfolyam lejárati ideje ({}) a létrehozás ideje ({}) utáni időpont-e.", rateExpirationTime, createdTime);
            Assert.assertTrue(rateExpirationTime.isAfter(createdTime), "Lejárati idő hiba!");

            response.then()
                    .assertThat()
                    .body("providedAmountType", equalTo("TARGET"))
                    .body("targetAmount", comparesEqualTo(new BigDecimal("100.00")));
        });
    }
}
