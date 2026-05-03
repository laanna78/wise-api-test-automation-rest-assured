package test.transaction;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Objects;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("TC-WISE-TRA-03: Lapozás ellenőrzése")
public class TcWiseTra03Test extends BaseOfWiseTests {
    @Test
    @Description("A második oldal lekérése és összehasonlítása a mentett első oldal azonosítóival.")
    public void listTransactionsPaginationTest() {

        // 1. LÉPÉS: Mentett ID-k beolvasása és listává alakítása
        String savedIdsRaw = ConfigReader.getProperty("first_five_ids");
        List<String> firstFiveIds = Arrays.asList(savedIdsRaw.split(","));
        String nextCursor = ConfigReader.getProperty("next_page_cursor");

        Assumptions.assumeTrue(nextCursor != null && !nextCursor.isEmpty(), "Nincs több elem, nincsen következő oldal!");

        // 2. LÉPÉS: A második oldal lekérése
        Response response = Allure.step("Lépés 1: Második oldal lekérése (size=5, offset=5)", () -> given()
                .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                .queryParam("size", 5)
                .queryParam("nextCursor", nextCursor)
                .contentType(JSON)
        .when()
                .get("/v1/profiles/" + ConfigReader.getProperty("expected_profile_id") + "/activities"));

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Átfedésmentesség és lista méret ellenőrzése, ID-k listázása", () -> {
            attachJson(response, "Második oldal tranzakciói");

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

            List<String> secondFiveIds = response.jsonPath().getList("activities.resource.id");

            // Ellenőrizzük, hogy a második oldal elemei nem szerepelnek az első oldal mentett listájában
            logger.info("--- Tranzakció ID-k összehasonlítása ---");
            logger.info("Első 5 ID (mentve az előző tesztesetből): {}", firstFiveIds);
            logger.info("Második 5 ID (aktuális lekérés): {}", secondFiveIds);
            boolean hasOverlap = !Collections.disjoint(firstFiveIds, secondFiveIds);

            assertThat("Átfedés található! A második oldal tartalmaz az első oldalról származó ID-t.",
                    hasOverlap, is(false));

            logger.info("Sikeres kurzor alapú lapozás. Nincs átfedés az első 5 mentett tranzakcióval.");
        });
    }
}
