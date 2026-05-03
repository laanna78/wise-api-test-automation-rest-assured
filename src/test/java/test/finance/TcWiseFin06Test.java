package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("TC-WISE-FIN-06: Egyenleg frissülés ellenőrzése - forrásszámla")
public class TcWiseFin06Test extends BaseOfWiseTests {
    @Test
    @Description("Annak validálása, hogy az átvezetés után a forrásszámla egyenlege az átutalt összeggel csökkent.")
    public void verifySourceBalanceDecreaseTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése az EUR egyenleg lekérdezéséhez", () -> {
            logger.info("EUR egyenleg ellenőrzése...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") +
                            "/balances/" + ConfigReader.getProperty("balance_id_eur"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Egyenleg csökkenés pontos validálása", () -> {
            attachJson(response, "Aktuális EUR egyenleg");

            BigDecimal balanceBefore = new BigDecimal(ConfigReader.getProperty("eur_balance_before"));
            BigDecimal sourceAmountValue = new BigDecimal(ConfigReader.getProperty("source_amount_value"));
            BigDecimal actualBalance = response.jsonPath().getObject("amount.value", BigDecimal.class);

            BigDecimal expectedBalance = balanceBefore.subtract(sourceAmountValue).setScale(2, RoundingMode.HALF_UP);
            BigDecimal formattedActualBalance = actualBalance.setScale(2, RoundingMode.HALF_UP);

            logger.info("Összeg validálása - Kiinduló egyenleg: {}, Levont összeg: {}, Várt egyenleg: {}, " +
                            "Aktuális egyenleg: {}", balanceBefore, sourceAmountValue, expectedBalance, formattedActualBalance);

            assertThat("Az EUR egyenleg nem a várt mértékben csökkent!",
                    formattedActualBalance.compareTo(expectedBalance) == 0);
        });
    }
}
