package test.transaction;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.*;

@DisplayName("TC-WISE-TRA-06: Szűrés dátum után")
public class TcWiseTra06Test extends BaseOfWiseTests {
    @Test
    @Description("Egy adott dátumidőt követő tranzakciók lekérése és a növekvő időrendi sorrend ellenőrzése.")
    public void filterTransactionsAfterDateTest() {
        String startDate = "2026-04-28T00:00:00Z";

        Response response = Allure.step("Lépés 1: GET kérés küldése a transfers végpontra createdDateStart szűrővel", () -> {
            logger.info("Tranzakciók lekérése a következő dátum után: {}...", startDate);
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .queryParam("createdDateStart", startDate)
                    .contentType(JSON)
            .when()
                    .get("/v1/transfers");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Dátum korlát és növekvő sorrend validálása", () -> {
            attachJson(response, "Dátum utáni szűrt tranzakciók");

            LocalDateTime startThreshold = OffsetDateTime.parse("2026-04-28T00:00:00Z").toLocalDateTime();

            List<String> createdDates = response.jsonPath().getList("created");

            List<String> validDates = createdDates.stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (!validDates.isEmpty()) {
                for (int i = 0; i < validDates.size(); i++) {
                    LocalDateTime actualDate = parseToLocalDateTime(validDates.get(i));

                    Assertions.assertFalse(actualDate.isBefore(startThreshold), String.format("A tranzakció dátuma (%s) korábbi, mint a kért kezdődátum (%s)!",
                            validDates.get(i), startDate));

                    if (i < validDates.size() - 1 && validDates.get(i+1) != null) {
                        LocalDateTime nextDate = parseToLocalDateTime(validDates.get(i + 1));
                        Assertions.assertFalse(actualDate.isAfter(nextDate),
                                String.format("Sorrendi hiba: %s > %s", validDates.get(i), validDates.get(i + 1)));
                    }
                }
            }

            logger.info("A szűrés sikeres, minden tranzakció {} utáni és a sorrend növekvő.", startDate);
        });
    }
}
