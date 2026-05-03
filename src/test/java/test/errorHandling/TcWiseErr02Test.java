package test.errorHandling;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-ERR-02: Hibakezelés - Jogosulatlan hozzáférés")
public class TcWiseErr02Test extends BaseOfWiseTests {
    @Test
    @Description("Egy idegen vagy nem létező kedvezményezett (Recipient) lekérdezése véletlenszerű ID-val. A várt eredmény HTTP 403 Forbidden.")
    public void unauthorizedRecipientAccessTest() {
        String randomRecipientId = "700975572";

        Response response = Allure.step("Lépés 1: GET kérés küldése egy idegen recipientId-val", () -> {
            logger.info("Próbálkozás idegen kedvezményezett lekérésével (ID: {})...", randomRecipientId);
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
                    .when()
                    .get("/v2/accounts/" + randomRecipientId);
        });

        Allure.step("Lépés 2: HTTP 403 Forbidden státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 403, step);
        });

        Allure.step("Lépés 3: Biztonsági hibaüzenet validálása", () -> {
            attachJson(response, "Hiba válasz - Unauthorized");

            response.then()
                    .body("error", equalTo("unauthorized"))
                    .body("message", equalTo("Unauthorized"))
                    .body("status", is(403))
                    .body("timestamp", notNullValue());

            logger.info("A rendszer sikeresen megvédte az adatokat: a hozzáférés megtagadva.");
        });
    }
}
