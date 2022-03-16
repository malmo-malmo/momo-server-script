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

import HTTPClient.NVPair
import groovy.json.JsonSlurper
import HTTPClient.Codecs

@RunWith(GrinderRunner)
class TestRunner {

	public static GTest test1
	public static GTest test2
	public static HTTPRequest request
	public static NVPair[] headers = []
	
	public static def slurper = new JsonSlurper()
	public static def toJSON = { slurper.parseText(it) }

	@BeforeProcess
	public static void beforeProcess() {
		HTTPRequestControl.setConnectionTimeout(300000)
		test1 = new GTest(1, "로그인")
		test2 = new GTest(2, "엑세스 토큰 재발급")
		request = new HTTPRequest()
		grinder.logger.info("before process.")
	}

	@BeforeThread
	public void beforeThread() {
		test1.record(this, "test1")
		test2.record(this, "test2")
		grinder.statistics.delayReports = true
		grinder.logger.info("before thread.")
	}

	private String accessToken;
	private String refreshToken;
	private String deviceCode = "device_code";

	@Before
	public void before() {
		headers = [ new NVPair("Content-type", "application/json;charset=UTF-8"), new NVPair("Authorization", "Bearer " + accessToken)]
		request.setHeaders(headers)
		grinder.logger.info("before. init headers and cookies")
	}

	@Test
	public void test1() {
		def map = [provider: "kakao", authorizationCode: "code", deviceCode: deviceCode]
		HTTPResponse response = request.POST("http://125.6.40.36:8080/api/oauth/login", map)
		
		def result = response.getBody(toJSON);
		accessToken = result.accessToken;
		refreshToken = result.refreshToken;

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}

	@Test
	public void test2() {
		def map = [refreshToken: refreshToken, deviceCode: deviceCode]
		HTTPResponse response = request.POST("http://125.6.40.36:8080/api/oauth/login/refresh", map)

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}
}
