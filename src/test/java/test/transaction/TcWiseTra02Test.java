package test.transaction;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-WISE-TRA-02: Megjelenített elemek számának beállítása")
public class TcWiseTra02Test extends BaseOfWiseTests {
    @Test
    @Description("A válaszban kapott elemek számának korlátozása a 'size' paraméter segítségével.")
    public void listTransactionsWithLimitTest() {

        Response response = Allure.step("Lépés 1: GET kérés küldése az activities végpontra size=5 paraméterrel", () -> {
            logger.info("Tranzakciók lekérése 5 elemre korlátozva a {} profilhoz...", ConfigReader.getProperty("expected_profile_id"));
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .queryParam("size", 5)
                    .contentType(JSON)
            .when()
                    .get("/v1/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/activities");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: A lista méretének és a sorrendnek a validálása, id-k mentése a következő tesztesethez", () -> {
            attachJson(response, "Limitált tranzakció lista");

            // Elemszám ellenőrzése
            response.then()
                    .body("activities", instanceOf(List.class))
                    .body("activities.size()", equalTo(5));

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

                    Assertions.assertTrue(isCorrectOrder, "A limitált tranzakciók nem csökkenő időrendben érkeztek!");
                }
            }

            List<String> firstFiveIds = response.jsonPath().getList("activities.resource.id");

            // Lista összefűzése Stringgé az id-k mentéséhez
            String idsToSave = String.join(",", firstFiveIds);
            ConfigReader.setProperty("first_five_ids", idsToSave);

            logger.info("Első öt tranzakció ID elmentve: {}", idsToSave);

            // Cursor elmentése a következő teszteset lapozás funkciójához
            ConfigReader.setProperty("next_page_cursor", response.jsonPath().getString("cursor"));

            logger.info("A lista pontosan 5 elemet tartalmaz és a sorrend megfelelő.");
        });
    }
}
