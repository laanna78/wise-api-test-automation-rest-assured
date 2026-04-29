package test;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import utils.ConfigReader;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public class BaseOfWiseTests {
    protected static final Logger logger = LogManager.getLogger();
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = ConfigReader.getProperty("base_url");

        RestAssured.filters(new AllureRestAssured(), new RequestLoggingFilter(), new ResponseLoggingFilter());
        logger.info("A tesztkörnyezet inicializálása megtörtént: " + RestAssured.baseURI);
    }

    @BeforeEach
    public void startLog(TestInfo testInfo) {
        logger.info(">>> Teszt indítása: " + testInfo.getDisplayName());
    }

    @AfterEach
    public void stopLog(TestInfo testInfo) {
        logger.info("<<< Teszt befejezve: " + testInfo.getDisplayName());
        logger.info("-------------------------------------------------------");
    }
}
