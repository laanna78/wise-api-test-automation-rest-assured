package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;


public class TcWiseFin07Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-07: Egyenleg frissülés ellenőrzése - célszámla",
            dependsOnMethods = "test.finance.TcWiseFin05Test.internalTransferEurToGbpTest")
    @Description("Annak validálása, hogy az átvezetés után a célszámla egyenlege az átutalt összeggel nőtt.")
    public void verifyTargetBalanceIncreaseTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése a GBP egyenleg lekérdezéséhez", () -> {
            logger.info("GBP egyenleg ellenőrzése...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") +
                            "/balances/" + ConfigReader.getProperty("balance_id_gbp"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Egyenleg növekedés pontos validálása", () -> {
            attachJson(response, "Aktuális GBP egyenleg");

            BigDecimal balanceBefore = new BigDecimal(ConfigReader.getProperty("gbp_balance_before"));
            BigDecimal targetAmountValue = new BigDecimal(ConfigReader.getProperty("target_amount_value"));
            BigDecimal actualBalance = response.jsonPath().getObject("amount.value", BigDecimal.class);

            BigDecimal expectedBalance = balanceBefore.add(targetAmountValue).setScale(2, RoundingMode.HALF_UP);
            BigDecimal formattedActualBalance = actualBalance.setScale(2, RoundingMode.HALF_UP);

            logger.info("Összeg validálása - Kiinduló egyenleg: {}, Hozzáadott összeg: {}, Várt egyenleg: {}, " +
                            "Aktuális egyenleg: {}", balanceBefore, targetAmountValue, expectedBalance, formattedActualBalance);

            assertThat("A GBP egyenleg nem a várt mértékben nőtt!",
                    formattedActualBalance.compareTo(expectedBalance) == 0);
        });
    }

}
