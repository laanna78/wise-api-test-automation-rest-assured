package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class TcWiseFin03Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-03: Kedvezményezettek listája")
    @Description("Kedvezményezettek listájának lekérdezése és a korábban létrehozott elemek jelenlétének ellenőrzése.")
    public void listRecipientsTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése a kedvezményezettek listázásához", () -> {
            logger.info("Kedvezményezettek listájának lekérése...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v2/accounts");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: A lista tartalmának validálása", () -> {
            attachJson(response, "Kedvezményezett lista");

            int idUsa = Integer.parseInt(ConfigReader.getProperty("recipient_id_usa"));
            int idEur = Integer.parseInt(ConfigReader.getProperty("recipient_id_eur"));

            response.then()
                    .assertThat()
                    .body("content", instanceOf(List.class))
                    .body("content.size()", greaterThanOrEqualTo(2))
                    .body("content.id", hasItems(idUsa, idEur));

            List<Integer> ids = response.jsonPath().getList("content.id");
            logger.info("Kinyert egyenleg azonosítók a content-ből: {}", ids);

            assertThat("A content lista üres!", ids.isEmpty(), is(false));
        });
    }
}
