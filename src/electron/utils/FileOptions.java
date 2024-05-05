package electron.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Simple methods for interaction with file. Contains: file loader,file
 * writer,JSON parser
 * 
 * @author Electron
 * @version 1.2F
 */
public class FileOptions {
	private static void log(String msg) {
		return;
	}

	private static void logerr(String msg) {
		logger.error(msg);
	}

	/**
	 * Method provides reading line by line
	 * 
	 * @param path - path of file Note: you can get the path using
	 *             yourfile.getPath() function
	 * @return List<String> lines
	 */
	public static List<String> getFileLines(String path) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
			logerr("Error: " + e.getMessage());
			logerr("Cause: " + e.getCause());
			return null;
		}
	}

	/**
	 * Method provides reading in one string with splitters chars
	 * 
	 * @param f        - file to read
	 * @param splitter - splitter string
	 * @return String result
	 */
	public static String getFileLineWithSeparator(List<String> path, String splitter) {
		List<String> lines = path;
		if (lines == null) {
			return "Error loading file: null";
		}
		String result = "";
		for (int i = 0; i < lines.size(); i++) {
			result = result + lines.get(i) + splitter;
		}
		return result;
	}

	public static String getInternalFileLineWithSeparator(InputStream url, String splitter) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(url));
		String about = "";
		while (reader.ready()) {
			about = about + reader.readLine() + splitter;
		}
		return about;
	}

	/**
	 * Method provides reading in one string
	 * 
	 * @param f - file to load
	 * @return String data
	 */
	public static String getFileLine(File f) {
		List<String> infile = getFileLines(f.getPath());
		String in = "";
		for (int i = 0; i < infile.size(); i++)
			in = String.valueOf(in) + (String) infile.get(i);
		return in;
	}

	/**
	 * Method provides write to file function
	 * 
	 * @param in - the string to be written
	 * @param f  - file to write
	 */
	public static void writeFile(String in, File f) {
		for (; !f.canWrite() && !f.canRead(); LockSupport.parkNanos(100L))
			;
		Writer fr = null;
		try {
			fr = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);

			fr.write(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean writeLines(List<String> lines, File file) {
		try {
			Files.write(Paths.get(file.getPath()), lines, StandardOpenOption.WRITE);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This method checks file for exists. If file didn't found it creates.
	 * 
	 * @param f - File to check
	 */
	public static void loadFile(File f) {
		if (f.exists()) {
			if (f.canRead() && f.canWrite()) {
				log("[FileOptions]: File " + f.getName() + " loaded.");
			} else {
				logerr("[FileOptions]: File " + f.getName() + " can't be read or wrote.");
				logerr("[FileOptions]: Please, check for other launched copy of this program and for args of file "
						+ f.getName());
				System.exit(1);
			}
		} else {
			log("[FileOptions]: File not found. Creating it...");
			try {
				f.createNewFile();
				log("[FileOptions]: File " + f.getName() + " created and loaded.");
			} catch (IOException e) {
				logerr(e.getLocalizedMessage());
				logerr("[FileOptions]: Please, create " + f.getName() + " yourself and try again. Bye.");
				System.exit(1);
			}
		}
	}

	public static void loadFileLite(File f) {
		if (f.exists()) {
			if (f.canRead() && f.canWrite()) {
				log("[FileOptions]: File " + f.getName() + " loaded.");
			} else {
				logerr("[FileOptions]: File " + f.getName() + " can't be read or wrote.");
				logerr("[FileOptions]: Please, check for other launched copy of this program and for args of file "
						+ f.getName());
			}
		} else {
			log("[FileOptions]: File not found. Creating it...");
			try {
				f.createNewFile();
				log("[FileOptions]: File " + f.getName() + " created and loaded.");
			} catch (IOException e) {
				logerr(e.getLocalizedMessage());
				logerr("[FileOptions]: Please, create " + f.getName() + " yourself and try again. Bye.");
			}
		}
	}

	/**
	 * JSON format checker & loader
	 * 
	 * @param d - string in JSON format
	 * @return Object JSON
	 */
	public static Object ParseJs(String d) {
		if (d == null) {
			logerr("Error loading JSON. Please, check configs. Program will exit. \nError message: input string = null");
			System.exit(1);
			return null;
		}
		try {
			Object obj = (new JSONParser()).parse(d);
			return obj;
		} catch (ParseException e) {
			logerr("Error loading JSON. Please, check configs. Program will exit. \nError message: " + e.getMessage());
			System.exit(1);
			return null;
		}
	}

	/**
	 * JSON format checker & loader
	 * 
	 * @param d - string in JSON format
	 * @return Object JSON
	 * @throws ParseException
	 */
	public static Object ParseJsThrought(String d) throws ParseException {
		Object obj = (new JSONParser()).parse(d);
		return obj;
	}
}
