package gov.cms.qpp.conversion;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import gov.cms.qpp.conversion.decode.DecodeResult;
import gov.cms.qpp.conversion.decode.placeholder.DefaultDecoder;
import gov.cms.qpp.conversion.encode.placeholder.DefaultEncoder;
import gov.cms.qpp.conversion.model.Encoder;
import gov.cms.qpp.conversion.model.Node;
import gov.cms.qpp.conversion.model.XmlDecoder;
import org.jdom2.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConverterTest {

	@Test
	public void testNonexistantFile() {
		String regex = Converter.wildCardToRegex("*.xml");
		String expect = ".*\\.xml";
		assertEquals(expect, regex);
	}

	@Test
	public void testWildCardToRegex_simpleFileWild() {
		String regex = Converter.wildCardToRegex("*.xml");
		String expect = ".*\\.xml";
		assertEquals(expect, regex);
	}

	@Test
	public void testWildCardToRegex_pathFileWild() {
		String regex = Converter.wildCardToRegex("path/to/dir/*.xml");
		String expect = ".*\\.xml";
		assertEquals(expect, regex);
	}

	@Test
	public void testWildCardToRegex_pathAllWild() {
		String regex = Converter.wildCardToRegex("path/to/dir/*");
		String expect = ".*";
		assertEquals(expect, regex);
	}

	@Test
	public void testWildCardToRegex_pathExtraWild() {
		String regex = Converter.wildCardToRegex("path/to/dir/*.xm*");
		String expect = ".*\\.xm.*";
		assertEquals(expect, regex);
	}

	@Test
	public void testWildCardToRegex_doubleStar() {
		String regex = Converter.wildCardToRegex("path/to/dir/**");
		String expect = ".";
		assertEquals(expect, regex);
	}

	@Test
	public void testWildCardToRegex_tooManyWild() {
		String regex = Converter.wildCardToRegex("path/*/*/*.xml");
		String expect = "";
		assertEquals(expect, regex);
	}

	@Test
	public void testExtractDir_none() {
		String regex = Converter.extractDir("*.xml");
		String expect = ".";
		assertEquals(expect, regex);
	}

	@Test
	public void testExtractDir_unix() {
		String regex = Converter.extractDir("path/to/dir/*.xml");
		String expect = "path" + File.separator + "to" + File.separator + "dir" + File.separator;
		assertEquals(expect, regex);
	}

	@Test
	public void testExtractDir_windows() {
		// testing the extraction not the building on windows
		String regex = Converter.extractDir("path\\to\\dir\\*.xml");
		// this test is running on *nix so expect this path while testing
		String expect = "path" + File.separator + "to" + File.separator + "dir" + File.separator;

		assertEquals(expect, regex);
	}

	@Test
	public void testManyPath_xml() {
		Collection<File> files = Converter.manyPath("src/test/resources/pathTest/*.xml");
		assertNotNull(files);
		assertEquals(3, files.size());

		File aFile = new File("src/test/resources/pathTest/a.xml");
		assertTrue(files.contains(aFile));
		File bFile = new File("src/test/resources/pathTest/a.xml");
		assertTrue(files.contains(bFile));
		File dFile = new File("src/test/resources/pathTest/subdir/d.xml");
		assertTrue(files.contains(dFile));
	}

	@Test
	public void testManyPath_doubleWild() {
		Collection<File> files = Converter.manyPath("src/test/resources/pathTest/*.xm*");
		assertNotNull(files);
		assertEquals(4, files.size());

		File cFile = new File("src/test/resources/pathTest/c.xmm");
		assertTrue(files.contains(cFile));
	}

	@Test
	public void testCheckPath_xml() {
		Collection<File> files = Converter.checkPath("src/test/resources/pathTest/*.xml");
		assertNotNull(files);
		assertEquals(3, files.size());

		Collection<File> file = Converter.checkPath("src/test/resources/pathTest/a.xml");
		assertNotNull(file);
		assertEquals(1, file.size());

		Collection<File> none = Converter.checkPath("notExist/a.xml");
		assertNotNull(none);
		assertEquals(0, none.size());

		Collection<File> nill = Converter.checkPath(null);
		assertNotNull(nill);
		assertEquals(0, nill.size());

		Collection<File> blank = Converter.checkPath("   ");
		assertNotNull(blank);
		assertEquals(0, blank.size());
	}

	@Test
	public void testManyPath_pathNotFound() {
		Collection<File> files = Converter.manyPath("notExist/*.xml");

		assertNotNull(files);
		assertEquals(0, files.size());
	}

	@Test
	public void testValidArgs() {
		Collection<File> files = Converter.validArgs(
				new String[]{"src/test/resources/pathTest/a.xml", "src/test/resources/pathTest/subdir/*.xml"});

		assertNotNull(files);
		assertEquals(2, files.size());

		File aFile = new File("src/test/resources/pathTest/a.xml");
		assertTrue(files.contains(aFile));
		File dFile = new File("src/test/resources/pathTest/subdir/d.xml");
		assertTrue(files.contains(dFile));
	}

	@Test
	public void testValidArgs_noFiles() {
		Collection<File> files = Converter.validArgs(new String[]{});

		assertNotNull(files);
		assertEquals(0, files.size());
	}

	@Test
	public void testMultiThreadRun_testSkipValidationToo() {
		long start = System.currentTimeMillis();

		Converter.main(new String[]{Converter.SKIP_VALIDATION,
				"src/test/resources/pathTest/a.xml",
				"src/test/resources/pathTest/subdir/*.xml"});

		long finish = System.currentTimeMillis();

		File aJson = new File("a.qpp.json");
		File dJson = new File("d.qpp.json");

		// a.qpp.json and d.qpp.json will not exist because the a.xml and d.xml
		// file will get validation
		assertTrue( aJson.exists() );
		assertTrue( dJson.exists() );

		aJson.deleteOnExit();
		dJson.deleteOnExit();

		System.out.println("Time to run two thread transform " + (finish - start));
	}

	@Test
	public void testDefaults() throws Exception {
		Converter.main(new String[]{Converter.SKIP_VALIDATION,
				"src/test/resources/converter/defaultedNode.xml"});

		File jennyJson = new File("defaultedNode.qpp.json");
		String content = new String( Files.readAllBytes( Paths.get( "defaultedNode.qpp.json" ) ) );

		assertTrue( content.contains("Jenny") );
		jennyJson.deleteOnExit();
	}

	@Test
	public void testSkipDefaults() throws Exception {
		Converter.main(new String[]{Converter.SKIP_VALIDATION,
				Converter.SKIP_DEFAULTS,
				"src/test/resources/converter/defaultedNode.xml"});

		File jennyJson = new File("defaultedNode.qpp.json");
		String content = new String( Files.readAllBytes( Paths.get( "defaultedNode.qpp.json" ) ) );

		assertFalse( content.contains("Jenny") );
		jennyJson.deleteOnExit();
	}

	@XmlDecoder(templateId = "867.5309")
	public static class JennyDecoder extends DefaultDecoder {
		public JennyDecoder() {
			super("default decoder for Jenny");
		}

		@Override
		protected DecodeResult internalDecode(Element element, Node thisnode) {
			thisnode.putValue("DefaultDecoderFor", "Jenny");
			thisnode.setId("867.5309");
			return DecodeResult.TreeFinished;
		}
	}

	@Encoder(templateId = "867.5309")
	public static class Jenncoder extends DefaultEncoder {
		public Jenncoder() {
			super("default encoder for Jenny");
		}
	}
}
