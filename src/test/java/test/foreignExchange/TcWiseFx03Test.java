package test.foreignExchange;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-FX-03: Azonos devizák közötti átvezetés")
public class TcWiseFx03Test extends BaseOfWiseTests {
    @Test
    @Description("Belső átvezetés kérésénél annak ellenőrzése, hogy az árfolyam 1.0, és BALANCE fizetésnél a díj 0.")
    public void sameCurrencyQuoteTest() {

        Response response = Allure.step("Lépés 1: POST kérés küldése azonos forrás és cél devizával (EUR)", () -> given()
                .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                .contentType(JSON)
                .body(Map.of(
                        "sourceCurrency", "EUR",
                        "targetCurrency", "EUR",
                        "sourceAmount", 100,
                        "targetAccount", Integer.parseInt(ConfigReader.getProperty("recipient_id_eur"))
                ))
                .post("/v3/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/quotes"));

        Allure.step("Lépés 2: HTTP 200 OK ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Árfolyam és díjmentesség validálása", () -> {
            attachJson(response, "Azonos deviza Quote válasz");

            // Árfolyam ellenőrzése (mindig 1.0 azonos devizánál)
            BigDecimal rate = new BigDecimal(response.jsonPath().get("rate").toString());
            Assertions.assertEquals(0, rate.compareTo(BigDecimal.ONE), "Azonos devizák esetén az árfolyamnak 1.0-nak kell lennie!");

            // Díjmentesség keresése a BALANCE opcióhoz
            List<Map<String, Object>> options = response.jsonPath().getList("paymentOptions");

            // Megkeressük a BALANCE típusú fizetési módot
            Map<String, Object> balanceOption = options.stream()
                    .filter(opt -> "BALANCE".equals(opt.get("payIn")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Nem található BALANCE fizetési opció a válaszban!"));

            @SuppressWarnings("unchecked")
            Map<String, Object> feeMap = (Map<String, Object>) balanceOption.get("fee");
            BigDecimal totalFee = new BigDecimal(feeMap.get("total").toString());

            // Validáljuk, hogy a díj 0
            Assertions.assertEquals(0, totalFee.compareTo(BigDecimal.ZERO),
                    "Saját számlák közötti BALANCE átvezetés esetén a díjnak 0-nak kell lennie!");

            // Összegek egyezősége díjmentes esetben
            BigDecimal source = new BigDecimal(balanceOption.get("sourceAmount").toString());
            BigDecimal target = new BigDecimal(balanceOption.get("targetAmount").toString());
            Assertions.assertEquals(0, source.compareTo(target), "Díjmentes átvezetésnél a forrás és cél összegnek meg kell egyeznie!");
        });

        Allure.step("Metaadatok ellenőrzése", () -> {
            response.then()
                    .body("sourceCurrency", equalTo("EUR"))
                    .body("targetCurrency", equalTo("EUR"))
                    .body("rateType", equalTo("FIXED"));
        });
    }
}
