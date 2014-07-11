package eu.compassresearch.core.s2c;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import eu.compassresearch.core.typechecker.TestUtil;

@RunWith(Parameterized.class)
public class TranlationTest
{
	private File file;

	public TranlationTest(String name, File file)
	{
		this.file = file;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getTests()
	{
		Collection<Object[]> tests = new Vector<Object[]>();

		FileFilter filter1= new FileFilter()
		{
			
			@Override
			public boolean accept(File pathname)
			{
				return pathname.getName().endsWith(".xmi") || pathname.isDirectory();
			}
		};
		
		final File root = new File("src/test/resources".replace('/', File.separatorChar));
		for (File f :collectFiles( root,filter1))
		{
			tests.add(new Object[] { f.getName(), f });
		}

		return tests;
	}
	
	public static Collection<File> collectFiles(File file, FileFilter filter)
	{
		Collection<File> files = new Vector<File>();
		if(file.isDirectory())
		{
			for (File f : file.listFiles(filter))
			{
				files.addAll(collectFiles(f,filter));	
			}
		}else
		{
			files.add(file);
		}
		return files;
	}
	
	
	@Test
	public void translateTest() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
	{
		check(translate(file));
	}
	
	
	
//	@Test
//	public void TranslateDwarfSimple() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
//	{
//		check(translate(new File("src/test/resources/Dwarf/DwarfSimple.xmi".replace('/', File.separatorChar))));
//	}
//	
//	@Test
//	public void TranslateS2CLight() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
//	{
//		check(translate(new File("src/test/resources/s2c-lite-initial-model2.xmi".replace('/', File.separatorChar))));
//	}
//	
//	@Test
//	public void TranslateS2CLightStatic() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
//	{
//		check(translate(new File("src/test/resources/s2c-lite-initial-model2-static.xmi".replace('/', File.separatorChar))));
//	}
//	
//	@Test
//	public void TranslateHSUV() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
//	{
//		check(translate(new File("src/test/resources/HSUV/HSUV.xmi".replace('/', File.separatorChar))));
//	}
	
//	@Test
//	public void TranslateBeoControlStreaming() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
//	{
//		check(translate(new File("src/test/resources/Streaming/BeoControlStreamingSosDesignModel.xmi".replace('/', File.separatorChar))));
//	}
//	
//	@Test
//	public void TranslateBeoControlStreamingUpdated() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
//	{
//		check(translate(new File("src/test/resources/Streaming/BeoControlStreamingSosDesignModel-updated.xmi".replace('/', File.separatorChar))));
//	}
//
//	@Test
//	public void TranslateInsiel() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException
//	{
//		check(translate(new File("src/test/resources/Insiel/insiel.xmi".replace('/', File.separatorChar))));
//	}
//	
	public File translate(File input) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException
	{
		final File output = new File("target".replace('/', File.separatorChar));
		return new S2cTranslator().translate(input, output,true);
	}
	
	public void check(File file) throws IOException
	{
		TestUtil.TypeCheckerResult res =  TestUtil.runTypeChecker(file.getAbsolutePath());
		
		if ( !res.parseErrors.isEmpty())
		{
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(outStream);


			for (String s : res.parseErrors)
			{
				out.println(s);
			}
			
			out.flush();
			Assert.assertTrue("Unexpected parse errors in model.\n\n"
					+ outStream.toString(), !res.parseErrors.isEmpty());

		} else
		if ( res.issueHandler.hasErrors())
		{
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(outStream);
			res.issueHandler.printErrors(out);
			out.flush();
			Assert.assertTrue("Unexpected type errors in model.\n\n"
					+ outStream.toString(), !res.issueHandler.hasErrors());

		} 
	}
}
