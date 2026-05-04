package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseFin10Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-10: Tranzakció státuszának lekérdezése", dependsOnMethods = "test.finance.TcWiseFin09Test.initiateExternalUsdTransferTest")
    @Description("Az elindított utalás aktuális állapotának lekérdezése a tranzakció azonosító alapján.")
    public void getTransferStatusTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése az utalás állapotának lekérdezéséhez", () -> {
            logger.info("Tranzakció státuszának lekérése...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v1/transfers/" + ConfigReader.getProperty("transfer_id_usa"));
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Státusz mező validálása", () -> {
            attachJson(response, "Tranzakció aktuális státusza");

            response.then()
                    .assertThat()
                    .body("status", equalTo("incoming_payment_waiting"));

            logger.info("A tranzakció státusza: {}", response.jsonPath().getString("status"));
        });
    }
}
