//package eu.compassresearch.core.typechecker;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.antlr.runtime.ANTLRInputStream;
//import org.antlr.runtime.CommonToken;
//import org.antlr.runtime.CommonTokenStream;
//import org.antlr.runtime.MismatchedTokenException;
//import org.antlr.runtime.RecognitionException;
//import org.overture.ast.analysis.AnalysisException;
//import org.overture.ast.definitions.PDefinition;
//
//import eu.compassresearch.ast.analysis.DepthFirstAnalysisCMLAdaptor;
//import eu.compassresearch.ast.program.AFileSource;
//import eu.compassresearch.ast.program.AInputStreamSource;
//import eu.compassresearch.ast.program.PSource;
//import eu.compassresearch.core.parser.CmlLexer;
//import eu.compassresearch.core.parser.CmlParser;
//import eu.compassresearch.core.parser.CmlParserError;
//import eu.compassresearch.core.typechecker.api.ICmlTypeChecker;
//import eu.compassresearch.core.typechecker.api.ITypeIssueHandler;
//import eu.compassresearch.core.typechecker.api.ITypeIssueHandler.CMLTypeError;
//import eu.compassresearch.core.typechecker.api.ITypeIssueHandler.CMLTypeWarning;
//
//public class TestUtil
//{
//
//	public static class TypeCheckerResult
//	{
//		public ITypeIssueHandler issueHandler;
//		public boolean parsedOk;
//		public boolean tcOk;
//		public List<String> parseErrors;
//		public PSource[] sources;
//	}
//
//	/**
//	 * TODO RWL: Write me
//	 * 
//	 * @param s
//	 * @return
//	 * @throws FileNotFoundException
//	 * @throws IOException
//	 */
//	public static TypeCheckerResult parse(PSource s)
//			throws FileNotFoundException, IOException
//	{
//
//		TypeCheckerResult result = new TypeCheckerResult();
//
//		ANTLRInputStream in = null;
//		if (s instanceof AFileSource)
//			in = new ANTLRInputStream(new FileInputStream(((AFileSource) s).getFile()));
//
//		if (s instanceof AInputStreamSource)
//			in = new ANTLRInputStream(((AInputStreamSource) s).getStream());
//
//		if (in == null)
//			return result;
//
//		CmlLexer lexer = new CmlLexer(in);
//		CommonTokenStream tokens = new CommonTokenStream(lexer);
//		CmlParser parser = new CmlParser(tokens);
//
//		try
//		{
//			s.setParagraphs(new LinkedList<PDefinition>());
//			for (PDefinition d : parser.source())
//			{
//				if (d != null)
//					s.getParagraphs().add(d);
//				else
//				{
//				}
//			}
//			result.parsedOk = true;
//			return result;
//		} catch (RecognitionException e)
//		{
//			String expectedToken = "";
//			CommonToken ct = null;
//			List<String> parseErrors = new LinkedList<String>();
//			result.parseErrors = parseErrors;
//			if (e instanceof MismatchedTokenException)
//			{
//				ct = (CommonToken) e.token;
//				MismatchedTokenException ee = (MismatchedTokenException) e;
//				expectedToken = CmlParser.tokenNames[ee.expecting];
//				parseErrors.add("Syntax error in " + s + " expecting '"
//						+ expectedToken + "' near '" + ct.getText()
//						+ "' at line " + e.line + " - " + ct.getStartIndex()
//						+ ":" + ct.getStopIndex());
//				return result;
//			}
//
//			if (e.token != null)
//			{
//				ct = (org.antlr.runtime.CommonToken) e.token;
//				parseErrors.add("Syntax error in " + s + " near '"
//						+ ct.getText() + "'. Error at line " + e.line + " - "
//						+ ct.getStartIndex() + ":" + ct.getStopIndex());
//			} else
//				parseErrors.add("Syntax error, expecting at line at line "
//						+ e.line + ".");
//			return result;
//		}
//	}
//
//	/**
//	 * TODO: RWL Write me
//	 * 
//	 * @param ss
//	 * @return
//	 * @throws FileNotFoundException
//	 * @throws IOException
//	 */
//	public static TypeCheckerResult runTypeChecker(List<PSource> ss)
//			throws FileNotFoundException, IOException
//	{
//		TypeCheckerResult result = new TypeCheckerResult();
//
//		for (PSource s : ss)
//		{
//			ANTLRInputStream in = null;
//			if (s instanceof AFileSource)
//				in = new ANTLRInputStream(new FileInputStream(((AFileSource) s).getFile()));
//
//			if (s instanceof AInputStreamSource)
//				in = new ANTLRInputStream(((AInputStreamSource) s).getStream());
//
//			CmlLexer lexer = new CmlLexer(in);
//			CommonTokenStream tokens = new CommonTokenStream(lexer);
//			CmlParser parser = new CmlParser(tokens);
//
//			try
//			{
//				List<PDefinition> forest = parser.source();
//				s.setParagraphs(new LinkedList<PDefinition>());
//				if (forest != null && parser.getErrors().isEmpty())
//				{
//					for (PDefinition def : forest)
//						if (def != null)
//							s.getParagraphs().add(def);
//					result.parsedOk = true;
//				} else
//				{
//					result.parsedOk = false;
//					List<String> parseErrors = new LinkedList<String>();
//					for (CmlParserError issue : parser.getErrors())
//					{
//						parseErrors.add(issue.toString());
//					}
//					result.parseErrors = parseErrors;
//
//					return result;
//				}
//			} catch (RecognitionException e)
//			{
//				String expectedToken = "";
//				CommonToken ct = null;
//				List<String> parseErrors = new LinkedList<String>();
//				result.parseErrors = parseErrors;
//				if (e instanceof MismatchedTokenException)
//				{
//					ct = (CommonToken) e.token;
//					MismatchedTokenException ee = (MismatchedTokenException) e;
//					if (ee.expecting >= 0
//							&& CmlParser.tokenNames.length > ee.expecting)
//						expectedToken = CmlParser.tokenNames[ee.expecting];
//					else
//						expectedToken = "unknown (-1)";
//					parseErrors.add("Syntax error in " + s + " expecting '"
//							+ expectedToken + "' near '" + ct.getText()
//							+ "' at line " + e.line + " - "
//							+ ct.getStartIndex() + ":" + ct.getStopIndex());
//					return result;
//				}
//
//				if (e.token != null)
//				{
//					ct = (org.antlr.runtime.CommonToken) e.token;
//					parseErrors.add("Syntax error in " + s + " near '"
//							+ ct.getText() + "'. Error at line " + e.line
//							+ " - " + ct.getStartIndex() + ":"
//							+ ct.getStopIndex());
//				} else
//					parseErrors.add("Syntax error, expecting at line at line "
//							+ e.line + ".");
//				return result;
//
//			}
//		}
//		ITypeIssueHandler issueHandler = VanillaFactory.newCollectingIssueHandle();
//		result.issueHandler = issueHandler;
//		List<PSource> cmlSources = new LinkedList<PSource>();
//		cmlSources.addAll(ss);
//		ICmlTypeChecker checker = VanillaFactory.newTypeChecker(cmlSources, issueHandler);
//
//		result.tcOk = checker.typeCheck();
//		result.sources = ss.toArray(new PSource[0]);
//
//		return result;
//	}
//
//	/**
//	 * TODO: Write me
//	 * 
//	 * @param file
//	 * @return
//	 * @throws IOException
//	 */
//	public static TypeCheckerResult runTypeChecker(String file)
//			throws IOException
//	{
//		TypeCheckerResult res = new TypeCheckerResult();
//
//		AFileSource fileSource = new AFileSource();
//
//		fileSource.setFile(new File(file));
//
//		List<PSource> cmlSources = Arrays.asList(new PSource[] { fileSource });
//
//		ANTLRInputStream in = new ANTLRInputStream(new FileInputStream(fileSource.getFile()));
//		CmlLexer lexer = new CmlLexer(in);
//		lexer.sourceFileName = fileSource.getFile().getName();
//		CommonTokenStream tokens = new CommonTokenStream(lexer);
//		CmlParser parser = new CmlParser(tokens);
//		parser.sourceFileName = lexer.sourceFileName;
//		try
//		{
//			fileSource.setParagraphs(parser.source());
//			res.parsedOk = true;
//		} catch (RecognitionException e)
//		{
//			// e.printStackTrace();
//			res.parsedOk = false;
//		}
//
//		if (res.parsedOk)
//		{
//			ITypeIssueHandler issueHandler = VanillaFactory.newCollectingIssueHandle();
//			res.issueHandler = issueHandler;
//			ICmlTypeChecker checker = VanillaFactory.newTypeChecker(cmlSources, issueHandler);
//
//			res.tcOk = checker.typeCheck();
//		}
//		res.sources = new PSource[] { fileSource };
//
//		return res;
//	}
//
//	/**
//	 * Returns a stack trace like string for the type errors.
//	 * 
//	 * @param tc
//	 * @param expectedTypesOk
//	 * @return
//	 */
//	public static String buildErrorMessage(ITypeIssueHandler tc,
//			boolean expectedTypesOk)
//	{
//		StringBuilder sb = new StringBuilder();
//		if (expectedTypesOk)
//		{
//			sb.append("Expected type checking to be successful, the following errors were unexpected:\n");
//			for (CMLTypeError error : tc.getTypeErrors())
//				sb.append(error.getLocation() + ": " + error.toString()
//						+ "\n------\n");
//			if (tc.getTypeErrors().size() > 0)
//				System.out.println(tc.getTypeErrors().get(0).getStackTrace());
//		} else
//		{
//			sb.append("Expected type checking to fail but it was successful.");
//		}
//		return sb.toString();
//	}
//
//	/**
//	 * Returns a stack trace like string for the type errors.
//	 * 
//	 * @param Type
//	 *            Issue handler used in TC
//	 * @param expectedNoWarnings
//	 * @return
//	 */
//	public static String buildWarningMessage(ITypeIssueHandler tc,
//			boolean expectedNoWarnings)
//	{
//		StringBuilder sb = new StringBuilder();
//		if (expectedNoWarnings)
//		{
//			sb.append("Expected no type checker warning, but the following warning were given:\n");
//			for (CMLTypeWarning warning : tc.getTypeWarnings())
//			{
//				sb.append(warning.getLocation() + ": " + warning.toString()
//						+ "\n------\n");
//				System.out.println(warning);
//			}
//
//		} else
//		{
//			sb.append("Expected type checker warning, but non were reported.");
//		}
//		return sb.toString();
//	}
//
//	public static <T> void addTestProgram(List<Object[]> col, String src,
//			Object... objs)
//	{
//		Object[] a = new Object[objs.length + 1];
//		a[0] = src;
//		System.arraycopy(objs, 0, a, 1, objs.length);
//		col.add(a);
//	}
//
//	@SuppressWarnings("serial")
//	public static Object findFirst(final Class<?> c, PSource s)
//			throws AnalysisException
//	{
//		class Holder
//		{
//			Object pointer;
//		}
//		final Holder h = new Holder();
//
//		DepthFirstAnalysisCMLAdaptor d = new DepthFirstAnalysisCMLAdaptor()
//		{
//
//			@Override
//			public void defaultInINode(org.overture.ast.node.INode node)
//					throws AnalysisException
//			{
//				if (node.getClass().equals(c))
//				{
//					h.pointer = node;
//					return;
//				}
//				super.defaultInINode(node);
//			}
//
//		};
//		s.apply(d);
//		return h.pointer;
//	}
//
//	public static String readFile(String file) throws IOException
//	{
//		File fin = new File(file);
//		InputStream is = new FileInputStream(fin);
//		byte[] buffer = new byte[is.available()];
//		is.read(buffer);
//		is.close();
//		return new String(buffer);
//	}
//
//	public static <T> void addFileProgram(List<Object[]> col, String filename,
//			Object... objs) throws IOException
//	{
//		String progDir = "../../docs/cml-examples/";
//		Object[] args = new Object[objs.length + 1];
//		args[0] = readFile(progDir + filename);
//		System.arraycopy(objs, 0, args, 1, objs.length);
//		col.add(args);
//	}
//
//	public static PSource makeSource(String cmlSource, String... names)
//	{
//		InputStream cmlSourceIn = new ByteArrayInputStream(cmlSource.getBytes());
//		AInputStreamSource source = new AInputStreamSource();
//		source.setOrigin(names != null && names.length > 0 ? names[0]
//				: "Test Parameter");
//		source.setStream(cmlSourceIn);
//		return source;
//	}
//
//}
