package test.finance;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseFin16Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-FIN-16: Kedvezményezett létrehozása - rossz IBAN")
    @Description("Kedvezményezett létrehozásának megkísérlése érvénytelen IBAN számmal.")
    public void createRecipientWithInvalidIbanTest() {

        Map<String, Object> details = new HashMap<>();
        details.put("legalType", "PRIVATE");
        details.put("iban", "DE893400440532013000");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("currency", "EUR");
        requestBody.put("type", "iban");
        requestBody.put("accountHolderName", "Rossz IBAN");
        requestBody.put("details", details);

        Response response = Allure.step("Lépés 1: POST kérés küldése érvénytelen IBAN-nal", () -> {
            logger.info("Kedvezményezett létrehozása rossz IBAN-nal...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
                    .body(requestBody)
            .when()
                    .post("/v1/accounts");
        });

        Allure.step("Lépés 2: HTTP 422 Unprocessable Entity státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 422, step);
        });

        Allure.step("Lépés 3: Hibaüzenet validálása", () -> {
            attachJson(response, "Szerver válasza a hibás IBAN-ra");

            response.then()
                    .assertThat()
                    .body("errors[0].code", equalTo("NOT_VALID"))
                    .body("errors[0].message", equalTo("Please specify a valid IBAN."))
                    .body("errors[0].path", equalTo("IBAN"));

            logger.info("A rendszer visszautasította az érvénytelen IBAN-t.");
        });
    }
}
