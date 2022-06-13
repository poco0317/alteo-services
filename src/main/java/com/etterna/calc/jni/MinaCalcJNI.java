package com.etterna.calc.jni;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

public class MinaCalcJNI {
	
	final static String LIB_NAME = "MinaCalcJNI";
	final static String WIN_EXT = ".dll";
	final static String NIX_EXT = ".so";
	
	static {
		String libPath = "";
				
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			libPath = LIB_NAME + WIN_EXT;
		} else {
			libPath = LIB_NAME + NIX_EXT;
		}
		
		URL url = MinaCalcJNI.class.getResource("/" + libPath);
		try {
			File tmpdir = Files.createTempDirectory("minacalcjnitmp").toFile();
			tmpdir.deleteOnExit();
			
			File tmpfile = new File(tmpdir, libPath);
			tmpfile.deleteOnExit();
			
			try (InputStream in = url.openStream()) {
				Files.copy(in, tmpfile.toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.load(tmpfile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public native String getCalcVersion();
	public native float[] minaSDCalc(String chartkey, float musicrate, float goal);

}
