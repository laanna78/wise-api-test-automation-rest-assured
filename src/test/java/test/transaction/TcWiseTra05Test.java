package test.transaction;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;
import test.BaseOfWiseTests;
import utils.ConfigReader;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.*;


public class TcWiseTra05Test extends BaseOfWiseTests {

    @Test(description = "TC-WISE-TRA-05: Szűrés két dátum között")
    @Description("Tranzakciók lekérdezése egy meghatározott időintervallumra és a növekvő sorrend ellenőrzése.")
    public void filterTransactionsByDateIntervalTest() {
        String startDate = "2026-04-25T00:00:00Z";
        String endDate = "2026-04-27T23:59:59Z";

        Response response = Allure.step("Lépés 1: GET kérés küldése dátum szűrőkkel", () -> {
            logger.info("Tranzakciók szűrése {} és {} között...", startDate, endDate);
            return given()
                    .header("Authorization", "Bearer " + ConfigReader.getProperty("auth_token"))
                    .queryParam("createdDateStart", startDate)
                    .queryParam("createdDateEnd", endDate)
                    .contentType(JSON)
            .when()
                    .get("/v1/transfers");
        });

        Allure.step("Lépés 2: HTTP 200 OK státuszkód ellenőrzése", (step) -> {
            checkStatusCode(response, 200, step);
        });

        Allure.step("Lépés 3: Intervallum és növekvő sorrend validálása", () -> {
            attachJson(response, "Dátumra szűrt tranzakciók");

            LocalDateTime startThreshold = parseToLocalDateTime(startDate);
            LocalDateTime endThreshold = parseToLocalDateTime(endDate);

            List<String> validDates = response.jsonPath().getList("created")
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();

            if (!validDates.isEmpty()) {
                for (int i = 0; i < validDates.size(); i++) {
                    String currentRaw = validDates.get(i);
                    LocalDateTime actualDate = parseToLocalDateTime(currentRaw);

                    Assert.assertFalse(actualDate.isBefore(startThreshold),
                            String.format("Hiba: %s korábbi, mint a kezdődátum (%s)!", currentRaw, startDate));

                    Assert.assertFalse(actualDate.isAfter(endThreshold),
                            String.format("Hiba: %s későbbi, mint a záródátum (%s)!", currentRaw, endDate));

                    if (i < validDates.size() - 1) {
                        String nextRaw = validDates.get(i + 1);
                        LocalDateTime nextDate = parseToLocalDateTime(nextRaw);

                        Assert.assertFalse(actualDate.isAfter(nextDate),
                                String.format("Sorrendi hiba: %s > %s", currentRaw, nextRaw));
                    }
                }
            }

            logger.info("A szűrés és a növekvő sorrend ellenőrzése sikeres.");
        });
    }
}
