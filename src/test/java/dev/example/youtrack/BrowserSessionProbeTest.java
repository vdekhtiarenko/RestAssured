package dev.example.youtrack;

import dev.example.youtrack.config.TestConfiguration;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("YouTrack browser session probes")
class BrowserSessionProbeTest {
    private static final Pattern PERMANENT_TOKEN = Pattern.compile("\\bperm:[A-Za-z0-9._-]{12,}");

    @Test
    @DisplayName("Browser login does not expose a permanent token in its session storage")
    void browserSessionDoesNotExposePermanentToken() {
        TestConfiguration configuration = TestConfiguration.load();
        WebDriver driver = login(configuration);
        try {
            @SuppressWarnings("unchecked")
            List<String> browserValues = (List<String>) ((JavascriptExecutor) driver).executeScript(
                    "const values = []; " +
                            "for (const storage of [localStorage, sessionStorage]) " +
                            "for (let i = 0; i < storage.length; i++) values.push(storage.getItem(storage.key(i))); " +
                            "return values;");
            String appToken = browserStoredToken(driver);

            boolean tokenFound = browserValues.stream().anyMatch(BrowserSessionProbeTest::containsPermanentToken)
                    || driver.manage().getCookies().stream()
                    .map(Cookie::getValue)
                    .anyMatch(BrowserSessionProbeTest::containsPermanentToken);
            assertFalse(tokenFound, "A permanent token should not be exposed in browser storage");
            System.out.println("Browser authentication token entry present=" + (appToken != null && !appToken.isBlank())
                    + "; permanent token found=" + tokenFound);
        } finally {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Probe whether the token stored by browser login authorizes the REST API")
    void probesRestApiWithBrowserStoredToken() {
        TestConfiguration configuration = TestConfiguration.load();
        WebDriver driver = login(configuration);
        try {
            String browserToken = browserStoredToken(driver);
            assertNotNull(browserToken, "No browser authentication token entry was found in localStorage");
            assertFalse(browserToken.isBlank(), "Browser authentication token entry was empty");
            boolean permanent = browserToken.startsWith("perm:");
            System.out.println("Browser token format: " + (permanent ? "permanent" : "not a permanent token"));

            Response response = given()
                    .header("Authorization", "Bearer " + browserToken)
                    .accept("application/json")
                    .get(configuration.baseUrl() + "/api/users/me?fields=id,login");

            if (response.statusCode() == 200) {
                Map<?, ?> user = response.as(Map.class);
                assertEquals(configuration.username(), user.get("login"),
                        "Browser-stored token should identify the logged-in user");
                System.out.println("Browser-stored token authorized GET /api/users/me: HTTP 200");
            } else {
                assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                        "Expected success or an authorization rejection, got HTTP " + response.statusCode());
                System.out.println("Browser-stored token did not authorize GET /api/users/me: HTTP "
                        + response.statusCode());
            }
        } finally {
            driver.quit();
        }
    }

    private static WebDriver login(TestConfiguration configuration) {
        assertFalse(configuration.username().isBlank(), "Set YOUTRACK_USERNAME in .env for browser probes");
        assertFalse(configuration.password().isBlank(), "Set YOUTRACK_PASSWORD in .env for browser probes");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1440,1000", "--disable-notifications");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(configuration.baseUrl() + "/login");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                    "input[placeholder='Username or Email'], input[name='username'], input[autocomplete='username']")))
                    .sendKeys(configuration.username());
            driver.findElement(By.cssSelector(
                    "input[placeholder='Password'], input[name='password'], input[type='password']"))
                    .sendKeys(configuration.password());
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//button[normalize-space()='Log in'] | //input[@type='submit']"))).click();
            wait.until(current -> String.valueOf(((JavascriptExecutor) current)
                    .executeScript("return document.body ? document.body.innerText : ''"))
                    .contains("Issues"));
            boolean passwordFormStillVisible = !driver.findElements(By.cssSelector("input[type='password']")).isEmpty();
            assertFalse(passwordFormStillVisible,
                    "YouTrack kept the login form visible after submit; browser credentials were rejected");
            return driver;
        } catch (RuntimeException e) {
            driver.quit();
            throw e;
        }
    }

    private static boolean containsPermanentToken(String value) {
        return value != null && PERMANENT_TOKEN.matcher(value).find();
    }

    private static String browserStoredToken(WebDriver driver) {
        return (String) ((JavascriptExecutor) driver).executeScript(
                "const key = Object.keys(localStorage).find(key => key.endsWith('-token')); " +
                        "return key ? localStorage.getItem(key) : null;");
    }
}
