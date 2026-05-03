package test.transaction;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-TRA-07: Szűrés jövőbeli dátumra")
public class TcWiseTra07Test extends BaseOfWiseTests {
    @Test
    @Description("Tranzakciók lekérdezése egy távoli jövőbeli dátumra. A várt eredmény egy üres lista.")
    public void filterTransactionsForFutureDateTest() {
        String futureDate = "2099-01-01T00:00:00Z";

        Response response = Allure.step("Lépés 1: GET kérés küldése a /v1/transfers végpontra jövőbeli createdDateStart paraméterrel", () -> {
            logger.info("Tranzakciók lekérése jövőbeli dátummal: {}...", futureDate);
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .queryParam("createdDateStart", futureDate)
                    .contentType(JSON)
            .when()
                    .get("/v1/transfers");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Üres lista és null kurzor validálása", () -> {
            attachJson(response, "Jövőbeli szűrés eredménye");

            response.then()
                    .body("$", is(empty()));

            logger.info("A szűrés sikeres: a rendszer helyesen üres listát adott vissza a jövőbeli dátumra.");
        });
    }
}
