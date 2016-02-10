package io.arusland.pel;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

import org.junit.Rule;

/**
 * Unit test for simple App.
 */

public class PELEngineTest {
	private final ReplacerFake replacerFake = new ReplacerFake();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testNormal1() {
		String input = "role:authorized";

		PELEngine eval = PELEngine.parse(input);

		String expression = eval.generateExpression(replacerFake);

		assertEquals("AuthUtil.isAuthorized()", expression);
	}

	@Test
	public void testNormal2() {
		String input = "role:authorized&&platform:android";

		PELEngine eval = PELEngine.parse(input);

		String expression = eval.generateExpression(replacerFake);

		assertEquals("AuthUtil.isAuthorized() && PlatformUtil.isAndroid()", expression);
	}

	@Test
	public void testNormal10() {
		String input = "!!(role:authorized && !!(platform:android)) || !role:admins ||!!config:allowedit";

		PELEngine eval = PELEngine.parse(input);

		String expression = eval.generateExpression(replacerFake);

		assertEquals(
				"!!(AuthUtil.isAuthorized() && !!(PlatformUtil.isAndroid())) || !AuthUtil.isAdmin() || !!ConfigUtil.hasConfig(\"allowedit\")",
				expression);
	}

	@Test
	public void testFailed1() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage("Operator cannot be last in expression: !");

		String input = "role:authorized!";

		PELEngine.parse(input);
	}

	@Test
	public void testFailed2() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage("Expression cannot be empty");

		String input = "";

		PELEngine.parse(input);
	}

	@Test
	public void testFailed3() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage("Invalid operator using: &&");

		String input = "&&role:authorized";

		PELEngine.parse(input);
	}

	@Test
	public void testFailed4() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage("Invalid operator using: &&");

		String input = "&&!role:authorized";

		PELEngine.parse(input);
	}

	@Test
	public void testFailed5() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage("Invalid operator using: &&");

		String input = "&&||role:authorized";

		PELEngine.parse(input);
	}

	@Test
	public void testFailed6() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage("Invalid operator using: &&");

		String input = "!&&role:authorized";

		PELEngine.parse(input);
	}

	@Test
	public void testFailed7() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage("Operator cannot be last in expression: !");

		String input = "!";

		PELEngine.parse(input);
	}
	
	@Test
	public void testFailed8() {
		exception.expect(IllegalStateException.class);
		exception.expectMessage("Operator cannot be last in expression: &&");

		String input = "role:authorized&&";

		PELEngine.parse(input);
	}
}
