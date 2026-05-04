package test.security;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseSec05Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-SEC-03: Biztonsági teszt - Nem megfelelő profil ID")
    @Description("Annak ellenőrzése, hogy a rendszer tiltja-e a hozzáférést (403 Forbidden) egy másik (nem létező " +
            "vagy idegen) profil egyenlegéhez.")
    public void unauthorizedProfileAccessTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése egy idegen profil ID-val (/v4/profiles/9999999/balances)", () -> {
            logger.info("Kísérlet idegen profil (99999999) egyenlegének elérésére...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v4/profiles/99999999/balances?types=STANDARD");
        });

        Allure.step("Lépés 2: HTTP 403 Forbidden státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 403, step);
        });

        Allure.step("Lépés 3: Hibaüzenet validálása (unauthorized)", () -> {
            attachJson(response, "Hibaüzenet");

            response.then()
                    .assertThat()
                    .body("error", equalTo("unauthorized"))
                    .body("message", equalTo("Unauthorized"));

            logger.info("A rendszer megfelelően blokkolta a jogosulatlan hozzáférést.");
        });
    }
}
