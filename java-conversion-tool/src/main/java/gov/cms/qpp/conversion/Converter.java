package gov.cms.qpp.conversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.qpp.conversion.decode.QppXmlDecoder;
import gov.cms.qpp.conversion.decode.XmlInputDecoder;
import gov.cms.qpp.conversion.decode.XmlInputFileException;
import gov.cms.qpp.conversion.encode.EncodeException;
import gov.cms.qpp.conversion.encode.JsonOutputEncoder;
import gov.cms.qpp.conversion.encode.QppOutputEncoder;
import gov.cms.qpp.conversion.model.Node;
import gov.cms.qpp.conversion.model.Validations;
import gov.cms.qpp.conversion.xml.XmlException;
import gov.cms.qpp.conversion.xml.XmlUtils;

public class Converter implements Callable<Integer> {
    static final Logger LOG = LoggerFactory.getLogger(Converter.class);

    static final int MAX_THREADS = 1;
    
    final File inFile;
    
    public Converter(File inFile) {
    	this.inFile = inFile;
	}
    
    @Override
    public Integer call() throws Exception {
    			
		if ( ! inFile.exists() ) {
			return 0; // it should if check prior to instantiation.
		}
		
		Validations.init();
		XmlInputDecoder fileDecoder = new QppXmlDecoder();
		try {
			Node decoded = fileDecoder.decode(XmlUtils.fileToDOM(inFile));
			
			JsonOutputEncoder encoder = new QppOutputEncoder();
			
			String name = inFile.getName().trim();
			System.out.println("Decoded template ID " + decoded.getId() + " from file '" + name + "'");
			//do something  with decode validations
			Validations.clear();
			Validations.init();
			
			String outName = name.replaceFirst("(?i)(\\.xml)?$", ".qpp.json");
			
			File outFile = new File(outName);
			System.out.println("Writing to file '" + outFile.getAbsolutePath() + "'");
			
			try (Writer writer = new FileWriter(outFile)) {
				encoder.setNodes(Arrays.asList(decoded));
				writer.write("Begin\n");
				encoder.encode(writer);
				writer.write("\nEnd\n");
				//do something  with encode validations

			} catch (IOException | EncodeException e) {
				throw new XmlInputFileException("Issues decoding/encoding.", e);
			} finally {
				Validations.clear();
			}
		} catch (XmlInputFileException | XmlException xe) {
			System.err.println("The file is not a QDRA-III xml document");
		}
		return null;
    }
    
    
    public static Collection<File> validArgs(String[] args) {
		if (args.length < 1) {
			System.err.println("No filename found...");
			return new LinkedList<>();
		}
		
		Collection<File> validFiles = new LinkedList<>();
		
		for (String arg : args) {
			validFiles.addAll( checkPath(arg) );
		}
		
		return validFiles;
    }
    
    public static Collection<File> checkPath(String path) {
    	Collection<File> existingFiles = new LinkedList<>();
    	
    	if (path == null || path.trim().length() == 0) {
    		return existingFiles;
    	}
    	
    	if ( path.contains("*") ) {
    		return manyPath(path);
    	}
    	
    	File file = new File(path);
    	if ( file.exists() ) {
    		existingFiles.add(file);
    	} else {
    		System.err.println(path + " does not exist.");
    	}
    	
    	return existingFiles;
    }
    
	public static Collection<File> manyPath(String path) {

    	File inDir = new File(extractDir(path));
    	String fileRegex = wildCardToRegex(path);
    	try {
    		Collection<File> existingFiles = FileUtils.listFiles(inDir, 
    			  new RegexFileFilter(fileRegex), DirectoryFileFilter.DIRECTORY);
    		return existingFiles;
    	} catch (Exception e) {
    		System.err.println("Cannot file path " + inDir+fileRegex);
    		return new LinkedList<>();
    	}
	}

	public static String extractDir(String path) {
		
		String[] parts = path.split("[\\/\\\\]");
		
		StringBuilder dirPath = new StringBuilder();
		for (String part : parts) {
			// append until a wild card
			if (part.contains("*")) {
				break;
			}
			dirPath.append(part).append(File.separator);
		}
		// if no path then use the current dir
		if (dirPath.length() == 0) {
			dirPath.append('.');
		}
		
		return dirPath.toString();
	}
	
	public static String wildCardToRegex(String path) {
		String regex = "";
		
		// this replace should work if the user does not give conflicting OS path separators
		String dirPath = extractDir(path);
		String wild = path;
		if (dirPath.length() > 1) {
			wild = wild.substring(dirPath.length());
		}

		String[] parts = wild.split("[\\/\\\\]");
		
		if (parts.length > 2) {
			System.err.println("Too many wild cards in " + path);
			return "";
		}
		String lastPart = parts[ parts.length-1 ];
		
		if ("**".equals(lastPart)) {
			regex = "."; // any and all files
		} else {
		
			// turn the last part into REGEX from file wild cards
			regex = lastPart.replaceAll("\\.", "\\\\.");
			regex = regex.replaceAll("\\*", ".*");
		}
		
		return regex;
	}
	
	public static void main(String[] args) {
		Collection<File> filenames = validArgs(args);
		processFiles(filenames);
	}

	private static void processFiles(Collection<File> filenames) {
		int threads = Math.min(MAX_THREADS, filenames.size());

		final ExecutorService execService = Executors.newFixedThreadPool(threads);
		
		try {
			CompletionService<Integer> completionService = 
				       new ExecutorCompletionService<Integer>(execService);
			
			startAllFileConversions(filenames, completionService);
			waitForAllToFinish(filenames.size(), completionService);
		} finally {
			execService.shutdown();
		}
	}

	private static void startAllFileConversions(Collection<File> filenames, CompletionService<Integer> completionService) {
		for(File filename : filenames) {
			Converter instance = new Converter(filename);
			completionService.submit(instance);
		}
	}

	private static void waitForAllToFinish(int count, CompletionService<Integer> completionService) {
		int finished = 0;
		while (finished < count) {
			Future<Integer> resultFuture = null;
			try {
				resultFuture = completionService.take();
				Integer result = resultFuture.get(); // TODO do something with result (and pick a good result)
				finished++;
				
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("Transformation interrupted. ");
				e.printStackTrace();
				throw new XmlInputFileException("Could not process file(s).", e);
			}
		}
	}

}
