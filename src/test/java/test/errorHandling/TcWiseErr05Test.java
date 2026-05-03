package test.errorHandling;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-ERR-05: Hibakezelés - Nem létező transfer lekérdezése")
public class TcWiseErr05Test extends BaseOfWiseTests {
    @Test
    @Description("Lekérdezés indítása egy nem létező transfer azonosítóval.")
    public void nonExistentTransferStatusTest() {
        String invalidTransferId = "2148495060";

        Response response = Allure.step("Lépés 1: GET kérés küldése érvénytelen transferId-val", () -> {
            logger.info("Transfer státusz lekérése nem létező ID-val: {}...", invalidTransferId);
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
                    .when()
                    .get("/v1/transfers/" + invalidTransferId);
        });

        Allure.step("Lépés 2: HTTP 404 Not Found státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 404, step);
        });

        Allure.step("Lépés 3: A hiba kódjának és üzenetének ellenőrzése", () -> {
            attachJson(response, "Hiba válasz - Transfer not found");

            response.then()
                    .body("errors[0].code", equalTo("transfer.not.found"))
                .body("errors[0].message", containsString("Transfer with such id is not found among your transfers"));

            logger.info("A validáció sikeres: a rendszer megfelelően kezelte a nem létező tranzakció ID-t.");
        });
    }
}
