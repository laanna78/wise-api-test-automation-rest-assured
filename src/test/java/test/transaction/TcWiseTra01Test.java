package test.transaction;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class TcWiseTra01Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-TRA-01: Tranzakciólista lekérdezése alapértelmezetten")
    @Description("Az összes tranzakció lekérése szűrés nélkül, a dátum szerinti csökkenő sorrend ellenőrzésével.")
    public void listTransactionsDefaultTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése a tranzakciólista (activities) végpontra", () -> {
            logger.info("Tranzakciók lekérése profilhoz: {}...", ConfigReader.getProperty("expected_profile_id"));
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .contentType(JSON)
            .when()
                    .get("/v1/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/activities");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Lista formátum és dátum szerinti sorrend validálása", () -> {
            attachJson(response, "Tranzakció lista");

            response.then()
                    .assertThat()
                    .body("activities", instanceOf(List.class))
                    .body("activities", not(empty()));

            List<String> createdDates = response.jsonPath().getList("activities.createdOn");

            List<String> validDates = createdDates.stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (validDates.size() < 2) {
                logger.info("Nincs elegendő elem (szám: {}) a sorrend ellenőrzéséhez.", validDates.size());
                return;
            }
            for (int i = 0; i < validDates.size() - 1; i++) {
                String current = validDates.get(i);
                String next = validDates.get(i + 1);

                boolean isCorrectOrder = current.compareTo(next) >= 0;

                if (!isCorrectOrder) {
                    logger.error("Sorrendi hiba detektálva! Index {}: {}, Index {}: {}", i, current, i + 1, next);
                }

                Assert.assertTrue(isCorrectOrder,
                        String.format("Hibás sorrend a(z) %d. és %d. elem között!", i, i + 1));
            }

            logger.info("A teljes lista ({} elem) sikeresen ellenőrizve, a sorrend megfelelő.", validDates.size());
        });
    }
}
