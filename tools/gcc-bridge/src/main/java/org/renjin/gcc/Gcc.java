package org.renjin.gcc;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleParser;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

public class Gcc {

	private GccEnvironment environment;
	private File pluginLibrary = new File("/home/alexander/dev/renjin-git/tools/gcc-plugin/renjin.so");
	
	private List<File> includeDirectories = Lists.newArrayList();

	private static final Logger LOGGER = Logger.getLogger(Gcc.class.getName());

	public Gcc() {
		if(Strings.nullToEmpty(System.getProperty("os.name")).toLowerCase().contains("windows")) {
			environment = new CygwinEnvironment();
		} else {
			environment = new UnixEnvironment();
		}
	}

	public GimpleCompilationUnit compileToGimple(File source) throws IOException {

		List<String> arguments = Lists.newArrayList();
		arguments.add("-c"); // compile only, do not link
		arguments.add("-S"); // stop at assembly generation
		//    command.add("-O9"); // highest optimization

		// Enable our plugin which dumps the Gimple as JSON
		// to standard out

		arguments.add("-fplugin=" + environment.toString(pluginLibrary));

		for(File includeDir : includeDirectories) {
			arguments.add("-I");
			arguments.add(environment.toString(includeDir));
		}

		arguments.add(environment.toString(source));

		LOGGER.info("Executing " + Joiner.on(" ").join(arguments));


		Process gcc = environment.startGcc(arguments);

		try {
			gcc.waitFor();
		} catch (InterruptedException e) {
			throw new GccException("Compiler interrupted");
		}

		String stderr = new String(ByteStreams.toByteArray(gcc.getErrorStream()));
		String json = new String(ByteStreams.toByteArray(gcc.getInputStream()));
		
		System.out.println(json);
		
		if(gcc.exitValue() != 0) {
			throw new GccException("Compilation failed:\n" + stderr);
		} else {
			java.lang.System.err.println(stderr);
		}

		GimpleParser parser = new GimpleParser();
		return parser.parse(new StringReader(json));
	}

	public void addIncludeDirectory(File path) {
		includeDirectories.add(path);
	}

}
