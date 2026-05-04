package test.security;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseSec02Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-SEC-02: Profiladatok lekérése hiányzó tokennel")
    @Description("Annak ellenőrzése, hogy az API elutasítja-e a kérést (401), ha hiányzik az Authorization fejléc.")
    public void missingTokenResponseTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése Authorization fejléc nélkül", () -> {
            logger.info("Kérés indítása token nélkül a /v1/profiles végpontra...");
            return given()
                    .contentType(JSON)
            .when()
                    .get("/v1/profiles");
        });

        Allure.step("Lépés 2: HTTP 401 Unauthorized státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 401, step);
        });

        Allure.step("Lépés 3: Hibaüzenetek validálása a válaszban", () -> {
            attachJson(response, "Hibaüzenet");

            response.then()
                    .assertThat()
                    .body("error", equalTo("missing_token"))
                    .body("error_description", equalTo("Missing token"));

            logger.info("A hibaüzenet validálása sikeres.");
        });
    }
}
