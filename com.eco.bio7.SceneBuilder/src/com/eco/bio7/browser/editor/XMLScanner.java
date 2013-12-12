package com.eco.bio7.browser.editor;

import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;

import com.eco.bio7.browser.editor.ColorManager;
import com.eco.bio7.browser.editor.IXMLColorConstants;
import com.eco.bio7.browser.editor.XMLWhitespaceDetector;

public class XMLScanner extends RuleBasedScanner {

	public XMLScanner(ColorManager manager) {
		IToken procInstr =
			new Token(
				new TextAttribute(
					manager.getColor(IXMLColorConstants.PROC_INSTR)));

		IRule[] rules = new IRule[2];
		//Add rule for processing instructions
		rules[0] = new SingleLineRule("<?", "?>", procInstr);
		// Add generic whitespace rule.
		rules[1] = new WhitespaceRule(new XMLWhitespaceDetector());
        
		setRules(rules);
	}
}
