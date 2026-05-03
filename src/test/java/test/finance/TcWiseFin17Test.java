package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-FIN-17: Kedvezményezett törlése - amerikai számla")
public class TcWiseFin17Test extends BaseOfWiseTests {
    @Test
    @Description("Az amerikai kedvezményezett külső számla eltávolítása DELETE kéréssel.")
    public void deleteUsaRecipientTest() {

        Response response = Allure.step("Lépés 1: DELETE kérés küldése a kedvezményezett törléséhez", () -> {
            logger.info("Amerikai kedvezményezett törlése (ID: {})...", ConfigReader.getProperty("recipient_id_usa"));
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .delete("/v2/accounts/" + ConfigReader.getProperty("recipient_id_usa"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Törlés visszaigazolásának validálása", () -> {
            attachJson(response, "Törlés utáni válasz");

            response.then()
                    .body("active", equalTo(false));

            ConfigReader.setProperty("recipient_id_usa", "");

            logger.info("A kedvezményezett sikeresen törölve (active=false).");
        });
    }
}
