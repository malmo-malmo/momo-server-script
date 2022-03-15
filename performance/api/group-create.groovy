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

import HTTPClient.NVPair
import groovy.json.JsonSlurper
import HTTPClient.Codecs

@RunWith(GrinderRunner)
class TestRunner {

	public static GTest test
	public static HTTPRequest request
	public static NVPair[] headers = []

	@BeforeProcess
	public static void beforeProcess() {
		HTTPRequestControl.setConnectionTimeout(300000)
		test = new GTest(1, "Test1")
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
		headers = [ new NVPair("Content-type", "application/json;charset=UTF-8") ]
		request.setHeaders(headers)
		grinder.logger.info("before. init headers and cookies")
	}

	@Test
	public void test() {
		NVPair param1 = new NVPair("name", "이름");
		NVPair param2 = new NVPair("category", "STOCK");
		NVPair param3 = new NVPair("isUniversity", "false");
		NVPair param4 = new NVPair("city", "SEOUL");
		NVPair param5 = new NVPair("district", "강남구");
		NVPair param6 = new NVPair("startDate", "2021-10-17");
		NVPair param7 = new NVPair("recruitmentCnt", "10");
		NVPair param8 = new NVPair("introduction", "설명");
		NVPair param9 = new NVPair("isOffline", "true");

		NVPair[] params = [param1, param2, param3, param4, param5, param6, param7, param8, param9];
		NVPair[] files = [new NVPair("image", "./resources/image.png")];

		def data = Codecs.mpFormDataEncode(params, files, headers)
		request.setHeaders(headers);

		HTTPResponse response = request.POST("http://125.6.40.36:8080/api/group", data)

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(201))
		}
	}
}
