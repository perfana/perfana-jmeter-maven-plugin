package com.lazerycode.jmeter.testrunner;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lazerycode.jmeter.configuration.JMeterProcessJVMSettings;

public class JMeterProcessBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(JMeterProcessBuilder.class);
	private int initialHeapSizeInMegaBytes;
	private int maximumHeapSizeInMegaBytes;
	private final String runtimeJarName;
	private String workingDirectory;
	private String javaRuntime;
	private List<String> userSuppliedArguments;
	private List<String> mainClassArguments = new ArrayList<>();

	public JMeterProcessBuilder(JMeterProcessJVMSettings settings, String runtimeJarName) {
	    JMeterProcessJVMSettings lSettings = settings;
		if (null == lSettings) {
		    lSettings = new JMeterProcessJVMSettings();
		}
		this.runtimeJarName = runtimeJarName;
		this.initialHeapSizeInMegaBytes = lSettings.getXms();
		this.maximumHeapSizeInMegaBytes = lSettings.getXmx();
		this.userSuppliedArguments = lSettings.getArguments();
		this.javaRuntime = lSettings.getJavaRuntime();
	}

	public void setWorkingDirectory(File workingDirectory) throws MojoExecutionException {
		try {
			this.workingDirectory = workingDirectory.getCanonicalPath();
		} catch (IOException ignored) {
			throw new MojoExecutionException("Unable to set working directory for JMeter process!", ignored);
		}
	}

	public void addArguments(List<String> arguments) {
		for (String argument : arguments) {
			this.mainClassArguments.add(argument);
		}
	}

	private String[] constructArgumentsList() {
		List<String> argumentsList = new ArrayList<>();
		argumentsList.add(javaRuntime);
		argumentsList.add(MessageFormat.format("-Xms{0}M", String.valueOf(this.initialHeapSizeInMegaBytes)));
		argumentsList.add(MessageFormat.format("-Xmx{0}M", String.valueOf(this.maximumHeapSizeInMegaBytes)));
		for (String argument : userSuppliedArguments) {
			argumentsList.add(argument);
		}

		argumentsList.add("-jar");
		argumentsList.add(runtimeJarName);
		for (String arg : mainClassArguments) {
			argumentsList.add(arg);
		}

		LOGGER.debug("Arguments for forked JMeter JVM: {}", argumentsList);

		return argumentsList.toArray(new String[argumentsList.size()]);
	}

	public Process startProcess() throws IOException {
	    String[] arguments = constructArgumentsList();
	    LOGGER.info("Starting process with:{}", Arrays.asList(arguments));
		ProcessBuilder processBuilder = new ProcessBuilder(arguments);
		processBuilder.redirectErrorStream(true);
		processBuilder.directory(new File(this.workingDirectory));
		return processBuilder.start();
	}
}
