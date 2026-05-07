package test;

import io.qameta.allure.Allure;
import io.restassured.filter.log.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import org.hamcrest.*;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.ConfigReader;
import io.restassured.RestAssured;
import org.apache.logging.log4j.*;

import static io.restassured.config.JsonConfig.jsonConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Properties;

public class BaseOfWiseTests {
    protected static final Logger logger = LogManager.getLogger();
    protected final ContentType JSON = ContentType.JSON;

    @BeforeSuite
    public void setup() {
        String baseUri = ConfigReader.getProperty("base_url");

        if (baseUri == null || baseUri.isEmpty())
            throw new RuntimeException("HIBA: A 'base_url' nem található sem a környezeti változókban, sem a " +
                    "config.properties fájlban!");

        RestAssured.baseURI = ConfigReader.getProperty("base_url");

        RestAssured.filters(new AllureRestAssured(), new RequestLoggingFilter(), new ResponseLoggingFilter());
        logger.info("A tesztkörnyezet inicializálása megtörtént: {}", RestAssured.baseURI);

        RestAssured.config = RestAssured.config()
                .jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    @BeforeMethod
    public void startLog(Method method) {
        String testName = method.getName();

        // Ha van kitöltött description, akkor azt íratjuk ki (ez felel meg a DisplayName-nek)
        Test testAnnotation = method.getAnnotation(Test.class);
        if (testAnnotation != null && !testAnnotation.description().isEmpty()) {
            testName = testAnnotation.description();
        }

        logger.info(">>> Teszt indítása: {}", testName);
    }

    @AfterSuite
    public void cleanUpAfterTests() {
        Allure.step("Tesztek utáni takarítás: config.properties alaphelyzetbe állítása", () -> {
            logger.info("Takarítás folyamatban...");

            List<String> keysToRemove = List.of(
                    "aud_balance_before",
                    "customer_transaction_id",
                    "eur_balance_before",
                    "fee_amounts_value",
                    "first_five_ids",
                    "gbp_balance_before",
                    "next_page_cursor",
                    "quote_id",
                    "quote_id_insufficient",
                    "recipient_id_eur",
                    "recipient_id_usa",
                    "source_amount_value",
                    "target_amount_value",
                    "transaction_id",
                    "transfer_id_usa",
                    "usd_balance_before"
            );

            try {
                String configPath = "src/test/resources/config.properties";
                File configFile = new File(configPath);

                if (configFile.exists()) {
                    Properties props = new Properties();

                    try (FileInputStream in = new FileInputStream(configFile)) {
                        props.load(in);
                    }

                    boolean changed = false;
                    for (String key : keysToRemove) {
                        if (props.containsKey(key)) {
                            props.remove(key);
                            logger.info("Kulcs eltávolítva: {}", key);
                            changed = true;
                        }
                    }

                    if (changed) {
                        try (FileOutputStream out = new FileOutputStream(configFile)) {
                            props.store(out, "Automatikus adattisztítás tesztfutás után.");
                        }
                        logger.info("A config.properties fájl sikeresen megtisztítva.");
                    }
                }
            } catch (IOException e) {
                logger.error("Hiba történt a takarítás során: {}", e.getMessage());
            }
        });
    }

    @AfterMethod
    public void stopLog(ITestResult result) {
        logger.info("<<< Teszt befejezve: {}", result.getMethod().getMethodName());
        logger.info("-------------------------------------------------------");
    }

    public Matcher<Object> validAmount() {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Object value) {
                if (!(value instanceof Number)) return false;

                BigDecimal bd = new BigDecimal(value.toString());

                return bd.scale() == 2
                        && !value.toString().contains(",")
                        && bd.compareTo(BigDecimal.ZERO) >= 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("valid amount (number, max 2 decimals, non-negative)");
            }
        };
    }

    protected void checkStatusCode(Response response, int expectedCode, Allure.StepContext stepContext) {
        int actualCode = response.getStatusCode();
        logger.info("Ellenőrzés - Várt státuszkód: {}, Kapott státuszkód: {}", expectedCode, actualCode);
        stepContext.parameter("Várt státusz", String.valueOf(expectedCode));
        stepContext.parameter("Kapott státusz", String.valueOf(actualCode));
        response.then().statusCode(expectedCode);
    }

    protected void attachJson(Response response, String attachmentLabel) {
        Allure.addAttachment(attachmentLabel, "application/json", response.asPrettyString());
    }

    protected static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendOptional(new DateTimeFormatterBuilder().appendLiteral('T').toFormatter())
            .appendOptional(new DateTimeFormatterBuilder().appendLiteral(' ').toFormatter())
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalStart().appendOffsetId().optionalEnd()
            .toFormatter();

    protected LocalDateTime parseToLocalDateTime(String rawDate) {
        if (rawDate == null) return null;
        try {
            // Megpróbáljuk alapként (időzóna nélkül)
            return LocalDateTime.parse(rawDate, FLEXIBLE_FORMATTER);
        } catch (Exception e) {
            // Ha nem megy, akkor zónával együtt, majd konvertáljuk
            return OffsetDateTime.parse(rawDate, FLEXIBLE_FORMATTER).toLocalDateTime();
        }
    }
}
