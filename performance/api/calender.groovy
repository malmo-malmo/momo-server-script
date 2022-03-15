package api

import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse
import org.ngrinder.http.cookie.Cookie
import org.ngrinder.http.cookie.CookieManager

import java.util.Date

@RunWith(GrinderRunner)
class TestRunner {

	public static GTest test
	public static HTTPRequest request
	public static Map<String, String> headers = [:]
	public static Map<String, Object> params = [:]
	public static List<Cookie> cookies = []

	@BeforeProcess
	public static void beforeProcess() {
		HTTPRequestControl.setConnectionTimeout(300000)
		test = new GTest(1, "User Schedule Performance Test")
		request = new HTTPRequest()
		grinder.logger.info("before process.")
	}

	@BeforeThread
	public void beforeThread() {
		test.record(this, "test")
		grinder.statistics.delayReports = true
		grinder.logger.info("before thread.")
	}

	@Before
	public void before() {
		request.setHeaders(headers)
		CookieManager.addCookies(cookies)
		grinder.logger.info("before. init headers and cookies")
	}

	@Test
	public void test() {
		def start = getRandomDate(1, 5)
		def end = getRandomDate(6, 12)
	
		HTTPResponse response = request.GET("http://125.6.40.36:8080/api/schedule/user-schedules?searchStartDate=" + start + "&searchEndDate=" + end)

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}
	
	private String getRandomDate(int min, int max) {
		Random rand = new Random()
        def month = String.format("%02d", min + rand.nextInt((max - min) + 1))
        def day = String.format("%02d", rand.nextInt(28) + 1)
        return "2021-" + month + "-" + day
    }
}
