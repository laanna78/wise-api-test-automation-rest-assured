package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;


public class TcWiseFin11Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-11: Tranzakció státuszának léptetése", dependsOnMethods = "test.finance.TcWiseFin09Test.initiateExternalUsdTransferTest")
    @Description("Az utalás manuális léptetése PROCESSING állapotba a Simulation API használatával.")
    public void simulateTransferProcessingTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése a szimulációs végpontra", () -> {
            logger.info("Tranzakció léptetése PROCESSING állapotba...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v1/simulation/transfers/" + ConfigReader.getProperty("transfer_id_usa") + "/processing");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: A válasz tartalmának naplózása", () -> {
            attachJson(response, "Szimuláció eredménye");

            response.then()
                    .assertThat()
                    .body("status", equalTo("processing"));

            logger.info("A tranzakció státusza: {}", response.jsonPath().getString("status"));
        });
    }
}
