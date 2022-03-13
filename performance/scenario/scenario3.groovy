package scenario

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
	public static GTest test3
	public static GTest test4

	public static HTTPRequest request
	public static NVPair[] headers = []

	public static def slurper = new JsonSlurper()
	public static def toJSON = { slurper.parseText(it) }

	@BeforeProcess
	public static void beforeProcess() {
		HTTPRequestControl.setConnectionTimeout(300000)
		test1 = new GTest(1, "로그인")
		test2 = new GTest(2, "내 모임 목록 조회")
		test3 = new GTest(3, "게시물 목록 조회")
		test4 = new GTest(4, "댓글 목록 조회")
		request = new HTTPRequest()

		grinder.logger.info("before process.")
	}

	@BeforeThread
	public void beforeThread() {
		test1.record(this, "test1")
		test2.record(this, "test2")
		test3.record(this, "test3")
		test4.record(this, "test4")
		grinder.statistics.delayReports = true
		grinder.logger.info("before thread.")
	}

	private String accessToken;
	private int groupId;
	private int postId;

	@Before
	public void before() {
		headers = [ new NVPair("Content-type", "application/json;charset=UTF-8"), new NVPair("Authorization", "Bearer " + accessToken)]
		request.setHeaders(headers)
		grinder.logger.info("before. init headers and cookies")
	}

	//로그인
	@Test
	public void test1() {
		def body = [provider: "kakao", authorizationCode: "code", deviceCode: "code"]
		HTTPResponse response = request.POST("http://125.6.40.36:8080/api/oauth/login", body)

		def result = response.getBody(toJSON);
		accessToken = result.accessToken;

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}

	//내 모임 목록 조회
	@Test
	public void test2() {
		HTTPResponse response = request.GET("http://125.6.40.36:8080/api/management/my-groups/details")

		def result = response.getBody(toJSON);
		groupId = result[0].id;

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}

	//게시물 목록 조회
	@Test
	public void test3() {
		HTTPResponse response = request.GET("http://125.6.40.36:8080/api/posts/paging?groupId=" + groupId + "&postType=NORMAL&page=0&size=10")
		def result = response.getBody(toJSON);
		postId = result[0].id;

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}

	//댓글 목록 조회
	@Test
	public void test4() {
		HTTPResponse response = request.GET("http://125.6.40.36:8080/api/comments/paging?postId=" + postId + "&page=0&size=10")

		if (response.statusCode == 301 || response.statusCode == 302) {
			grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
		} else {
			assertThat(response.statusCode, is(200))
		}
	}
}
