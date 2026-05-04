package test.errorHandling;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseErr04Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-ERR-04: Hibakezelés - HTTP Metódus hiba")
    @Description("GET hívás küldése a quotes végpontra, amely csak a POST metódust támogatja.")
    public void methodNotAllowedTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése a POST-alapú quotes végpontra", () -> {
            logger.info("GET kérés indítása a quotes végpontra (nem támogatott metódus)...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v3/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/quotes");
        });

        Allure.step("Lépés 2: HTTP 404 Not Found státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 404, step);
        });

        Allure.step("Lépés 3: Hibaobjektum tartalmának ellenőrzése", () -> {
            attachJson(response, "Hiba válasz - Not Found");

            response.then()
                    .assertThat()
                    .body("message", equalTo("Resource not found"))
                    .body("status", equalTo("404"))
                    .body("error", equalTo("Not Found"));

            logger.info("A rendszer helyesen 404-es hibát adott a nem támogatott GET metódusra.");
        });
    }
}
