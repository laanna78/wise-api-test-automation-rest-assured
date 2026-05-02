package test.balance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("TC-WISE-BAL-06: Összes egyenleg listázása")
public class TcWiseBal06Test extends BaseOfWiseTests {
    @Test
    @Description("A profilhoz tartozó összes aktív egyenleg lekérése és a lista tartalmának ellenőrzése.")
    public void listAllBalancesTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése az összes egyenleg lekéréséhez", () -> {
            logger.info("Összes egyenleg lekérése a típus szűrésével (STANDARD)...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .queryParam("types", "STANDARD")
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/balances");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: A válaszlista és a kötelező elemek validálása", () -> {
            attachJson(response, "Egyenleg lista");

            response.then()
                    .body("$", instanceOf(List.class))
                    .body("$.size()", greaterThan(0))
                    .body("id", hasItems(
                            Integer.parseInt(ConfigReader.getProperty("balance_id_eur")),
                            Integer.parseInt(ConfigReader.getProperty("balance_id_usd")),
                            Integer.parseInt(ConfigReader.getProperty("balance_id_aud")),
                            Integer.parseInt(ConfigReader.getProperty("balance_id_gbp")),
                            Integer.parseInt(ConfigReader.getProperty("balance_id_huf"))
                    ))
                    .body("currency", hasItems("EUR", "GBP", "AUD", "USD", "HUF"));

            List<Integer> ids = response.jsonPath().getList("id");
            logger.info("Kinyert egyenleg azonosítók: {}", ids);

            assertThat("A visszaadott lista üres!", ids.isEmpty(), is(false));
        });
    }
}
