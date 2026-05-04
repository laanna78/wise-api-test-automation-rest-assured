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


public class TcWiseTra04Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-TRA-04: Szűrés befejezett státuszra")
    @Description("Csak a befejezett (COMPLETED) státuszú tranzakciók listázása és validálása.")
    public void filterCompletedTransactionsTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése az activities végpontra status=COMPLETED paraméterrel", () -> {
            logger.info("Befejezett tranzakciók lekérése a {} profilhoz...", ConfigReader.getProperty("expected_profile_id"));
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .queryParam("status", "COMPLETED")
                    .contentType(JSON)
            .when()
                    .get("/v1/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/activities");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Státuszok és időrendi sorrend validálása", () -> {
            attachJson(response, "Szűrt tranzakció lista (COMPLETED)");

            // Ellenőrizzük, hogy minden kapott tranzakció státusza COMPLETED
            response.then()
                    .assertThat()
                    .body("activities.status", everyItem(equalTo("COMPLETED")));

            // Időrendi sorrend ellenőrzése (createdOn mező alapján)
            List<String> createdDates = response.jsonPath().getList("activities.createdOn");
            List<String> validDates = createdDates.stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (validDates.size() >= 2) {
                for (int i = 0; i < validDates.size() - 1; i++) {
                    String current = validDates.get(i);
                    String next = validDates.get(i + 1);

                    boolean isCorrectOrder = current.compareTo(next) >= 0;

                    if (!isCorrectOrder) {
                        logger.error("Sorrendi hiba a limitált listában! Index {}: {}, Index {}: {}", i, current, i + 1, next);
                    }

                    Assert.assertTrue(isCorrectOrder, "A limitált tranzakciók nem csökkenő időrendben érkeztek!");
                }
            }

            logger.info("A szűrés sikeres: minden tranzakció COMPLETED státuszú és a sorrend megfelelő.");
        });
    }
}
