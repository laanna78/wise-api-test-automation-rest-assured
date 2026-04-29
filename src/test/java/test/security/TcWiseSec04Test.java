package test.security;

import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TC-WISE-SEC-04: Trace ID jelenlétének ellenőrzése")
public class TcWiseSec04Test extends BaseOfWiseTests {

    @Test
    @Description("Annak ellenőrzése, hogy a válasz fejléc tartalmazza-e az x-trace-id-t")
    public void traceIdPresenceInHeaderTest() {
        Response response = Allure.step("Lépés 1: GET kérés küldése a /v1/profiles végpontra", () -> {
            logger.info("Kérés indítása a profil végpontra a Trace ID ellenőrzéséhez...");
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(ContentType.JSON)
            .when()
                    .get("/v1/profiles");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            logger.info("Státuszkód ellenőrzése...");
            int code = response.getStatusCode();
            step.parameter("Várt státuszkód", "200");
            step.parameter("Kapott státuszkód", String.valueOf(code));
            response.then().statusCode(200);
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

            assertNotNull(traceId, "Az x-trace-id fejléc hiányzik!");
            assertFalse(traceId.isEmpty(), "Az x-trace-id fejléc üres!");
        });
    }
}
