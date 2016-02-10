package io.arusland.pel;

import io.arusland.pel.PELEvaluator.Lexem;
import io.arusland.pel.PELEvaluator.Replacer;

public class ReplacerFake implements Replacer {
	@Override
	public String replace(Lexem lexem) {

		if ("role:authorized".equals(lexem.expressionValue())) {
			return "AuthUtil.isAuthorized()";
		} else if ("platform:android".equals(lexem.expressionValue())) {
			return "PlatformUtil.isAndroid()";
		} else if ("role:admins".equals(lexem.expressionValue())) {
			return "AuthUtil.isAdmin()";
		} else if ("config:allowedit".equals(lexem.expressionValue())) {
			return "ConfigUtil.hasConfig(\"allowedit\")";
		}

		throw new IllegalStateException("Invalid lexem: " + lexem);
	}
}
