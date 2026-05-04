package test.security;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;

public class TcWiseSec04Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-SEC-04: Trace ID jelenlétének ellenőrzése")
    @Description("Annak ellenőrzése, hogy a válasz fejléc tartalmazza-e az x-trace-id-t")
    public void traceIdPresenceInHeaderTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése a /v1/profiles végpontra", () -> {
            logger.info("Kérés indítása a profil végpontra a Trace ID ellenőrzéséhez...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v1/profiles");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: x-trace-id fejléc kinyerése és validálása", (step) -> {
            logger.info("x-trace-id fejléc keresése a válaszban...");
            String traceId = response.getHeader("x-trace-id");

            if (traceId != null) {
                step.parameter("Kinyert Trace ID", traceId);
                logger.info("SIKER: Az x-trace-id megtalálható. Értéke: {}", traceId);
            } else {
                logger.error("HIBA: Az x-trace-id nem található a fejlécben!");
            }

            Assert.assertNotNull(traceId, "Az x-trace-id fejléc hiányzik!");
            Assert.assertFalse(traceId.isEmpty(), "Az x-trace-id fejléc üres!");
        });
    }
}
