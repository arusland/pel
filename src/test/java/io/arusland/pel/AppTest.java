package io.arusland.pel;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */

public class AppTest extends TestCase {
	private final ReplacerFake replacerFake;

	public AppTest(String testName) {
		super(testName);

		replacerFake = new ReplacerFake();
	}

	public void testNormal1() {
		String input = "role:authorized";

		PELEvaluator eval = PELEvaluator.parse(input);

		String expression = eval.generateExpression(replacerFake);

		assertEquals("AuthUtil.isAuthorized()", expression);
	}
	
	public void testNormal2() {
		String input = "role:authorized&&platform:android";

		PELEvaluator eval = PELEvaluator.parse(input);

		String expression = eval.generateExpression(replacerFake);

		assertEquals("AuthUtil.isAuthorized() && PlatformUtil.isAndroid()", expression);
	}
	
	
	public void testNormal10() {
		String input = "!!(role:authorized && !!(platform:android)) || !role:admins ||!!config:allowedit";

		PELEvaluator eval = PELEvaluator.parse(input);

		String expression = eval.generateExpression(replacerFake);

		assertEquals("!!(AuthUtil.isAuthorized() && !!(PlatformUtil.isAndroid())) || !AuthUtil.isAdmin() || !!ConfigUtil.hasConfig(\"allowedit\")",
				expression);
	}
}
