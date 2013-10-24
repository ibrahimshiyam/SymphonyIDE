//package eu.compassresearch.core.typechecker;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//import org.junit.runners.Parameterized.Parameters;
//
//import eu.compassresearch.ast.program.PSource;
//import eu.compassresearch.core.typechecker.TestUtil.TypeCheckerResult;
//import eu.compassresearch.core.typechecker.api.ICmlTypeChecker;
//import eu.compassresearch.core.typechecker.api.ITypeIssueHandler;
//import eu.compassresearch.core.typechecker.api.ITypeIssueHandler.CMLTypeError;
//import eu.compassresearch.core.typechecker.api.TypeErrorMessages;
//import eu.compassresearch.core.typechecker.secondedition.TestConverter;
//
//@RunWith(value = Parameterized.class)
//public class CmlTypeCheckerTestCaseMultipleSources
//{
//
//	@Parameters
//	public static Collection<Object[]> parameter()
//	{
//
//		List<Object[]> testData = new LinkedList<Object[]>();
//		// 0
//		addTestProgram(testData, "class test1 = begin values a : int = 42 end", "class test2 = begin values b : int = test1`a + 1 end", true, new String[0]);
//		// 1
//		addTestProgram(testData, "class test1 = begin state public a : int := 1 end", "class test2 = begin state b : test1 end", true, new String[0]);
//		// 2
//		addTestProgram(testData, "class BigInteger = begin state ints:seq of int end", "class Numbers = begin state nums : seq of BigInteger end", true, new String[0]);
//		// 3
//		addTestProgram(testData, "class BigInteger = begin state ints:seq of int end", "class Numbers = begin state nums : seq of BigErrteger end", false, new String[] { TypeErrorMessages.EXPECTED_TYPE_DEFINITION.customizeMessage("BigErrteger") });
//		// 4
//		addTestProgram(testData, "class A = begin operations O: () ==> int O() == (return (42)) end", "class B = begin state b : A c : int := (b.O()) end", true, new String[0]);
//		// 5
//		addTestProgram(testData, "class A = begin operations O: () ==> int O() == (return (42)) end", "class B = begin values f : A = nil end", true, new String[0]);
//		// 6
//		addTestProgram(testData, "class A = begin operations f: () ==> int f() == (return (10)) end", "class B = begin state a : A operations g: () ==> int g() == (return (a.f())) end", true, new String[0]);
//		// 7
//		addTestProgram(testData, "class A = begin operations f: () ==> int f() == (return (10)) end", "class B = begin values a : A = nil functions g: int -> int g(i) == i + a.f() end", false, new String[0]);
//		// 8
//		addTestProgram(testData, "class A = begin operations f: () ==> int f() == (return (10)) end", "class B = begin values a : A = nil operations g: int ==> int g(i) == return i + a.f() end", true, new String[0]);
//
//		return testData;
//	}
//	
//	static int index = 2;
//	static final  String SUITE_NAME = "classes";
//	
//	public static <T> void addTestProgram(List<Object[]> col, String src,
//			Object... objs)
//	{
//		TestUtil.addTestProgram(col,src,objs);
//		TestConverter.convert("src/test/resources/", SUITE_NAME, index++, src,true,(boolean)objs[1]);
//	}
//	
//	
//
//	private PSource source1;
//	private PSource source2;
//	private boolean tcOK;
//	private String[] expectedErrors;
//
//	public CmlTypeCheckerTestCaseMultipleSources(String s1, String s2,
//			boolean tcOK, String[] errors)
//	{
//		this.source1 = TestUtil.makeSource(s1, "Source 1");
//		this.source2 = TestUtil.makeSource(s2, "Source 2");
//		this.tcOK = tcOK;
//		this.expectedErrors = errors;
//	}
//
//	@Test
//	public void test() throws IOException
//	{
//
//		TypeCheckerResult s1ok = TestUtil.parse(source1);
//		TypeCheckerResult s2ok = TestUtil.parse(source2);
//
//		Assert.assertTrue("Expected source 1 to parse, but it did not.", s1ok.parsedOk);
//
//		Assert.assertTrue("Expected source 2 to parse, but it did not.", s2ok.parsedOk);
//
//		ITypeIssueHandler typeErrors = VanillaFactory.newCollectingIssueHandle();
//		ICmlTypeChecker typeChecker = VanillaFactory.newTypeChecker(Arrays.asList(new PSource[] {
//				source1, source2 }), typeErrors);
//
//		boolean tcResult = typeChecker.typeCheck();
//
//		if (tcOK)
//		{
//			Assert.assertTrue(TestUtil.buildErrorMessage(typeErrors, tcOK), tcResult);
//		} else
//		{
//			Assert.assertFalse("Expected type checking to fail, but it did not.", tcResult);
//			HashSet<String> actualErrors = new HashSet<String>();
//			for (CMLTypeError e : typeErrors.getTypeErrors())
//				actualErrors.add(e.getDescription());
//			for (String expectedError : expectedErrors)
//				Assert.assertTrue("Expected to find error message: "
//						+ expectedError + " but it was not found.", actualErrors.contains(expectedError));
//		}
//
//	}
//
//}
