package test;

import io.qameta.allure.Allure;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import utils.ConfigReader;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import static io.restassured.config.JsonConfig.jsonConfig;

import java.math.BigDecimal;

public class BaseOfWiseTests {
    protected static final Logger logger = LogManager.getLogger();
    protected final ContentType JSON = ContentType.JSON;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ConfigReader.getProperty("base_url");

        RestAssured.filters(new AllureRestAssured(), new RequestLoggingFilter(), new ResponseLoggingFilter());
        logger.info("A tesztkörnyezet inicializálása megtörtént: {}", RestAssured.baseURI);

        RestAssured.config = RestAssured.config()
                .jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    @BeforeEach
    public void startLog(TestInfo testInfo) {
        logger.info(">>> Teszt indítása: " + testInfo.getDisplayName());
    }

    @AfterEach
    public void stopLog(TestInfo testInfo) {
        logger.info("<<< Teszt befejezve: {}", testInfo.getDisplayName());
        logger.info("-------------------------------------------------------");
    }

    public static Matcher<Object> validAmount() {
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
        logger.info("Ellenőrzés - Várt: {}, Kapott: {}", expectedCode, actualCode);
        stepContext.parameter("Várt státusz", String.valueOf(expectedCode));
        stepContext.parameter("Kapott státusz", String.valueOf(actualCode));
        response.then().statusCode(expectedCode);
    }

    protected void attachJson(Response response, String attachmentLabel) {
        Allure.addAttachment(attachmentLabel, "application/json", response.asPrettyString());
    }
}
