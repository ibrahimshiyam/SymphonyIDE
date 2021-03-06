package eu.compassresearch.pog.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;
import org.overture.pog.pub.IPogAssistantFactory;
import org.overture.pog.pub.IProofObligation;
import org.overture.pog.pub.IProofObligationList;

import eu.compassresearch.core.analysis.pog.utility.PogPubUtil;
import eu.compassresearch.core.analysis.pog.visitors.CmlPogAssistantFactory;
import eu.compassresearch.pog.tests.utils.TestInputHelper;

public class LocaleExtractTest {

	IPogAssistantFactory af;
	
	@Before
	public void setup() {
		af = new CmlPogAssistantFactory();
	}

	@Test
	public void test_ActionProc() throws IOException, AnalysisException {
		String testmodel = "src/test/resources/basic/other/localeProcess.cml";
		String result = "Locale";

		List<INode> ast = TestInputHelper.getAstFromName(testmodel);
		List<String> actual = new LinkedList<String>();
		IProofObligationList ipol = PogPubUtil.generateProofObligations(ast);
		for (IProofObligation ipo : ipol) {
			actual.add(ipo.getLocale());
		}
		List<String> expected = new LinkedList<String>();
		expected.add(result);
		assertEquals(expected, actual);

	}

	@Test
	public void test_GlobalFunc() throws IOException, AnalysisException {
		String testmodel = "src/test/resources/basic/other/localeGlobalValue.cml";
		String result = null;

		
		List<INode> ast = TestInputHelper.getAstFromName(testmodel);
		List<String> actual = new LinkedList<String>();
		IProofObligationList ipol = PogPubUtil.generateProofObligations(ast);
		for (IProofObligation ipo : ipol) {
			actual.add(ipo.getLocale());
		}
		List<String> expected = new LinkedList<String>();
		expected.add(result);
		assertEquals(expected, actual);

	}
}
