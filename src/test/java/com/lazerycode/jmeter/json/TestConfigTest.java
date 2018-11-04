package com.lazerycode.jmeter.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

public class TestConfigTest {

	private String testConfigFile = "/config.json";
	private String tempdir = System.getProperty("java.io.tmpdir");

	@Test
	public void createConfigFromResourceFile() throws MojoExecutionException, URISyntaxException {
		URL configFile = this.getClass().getResource(testConfigFile);
		File testConfigJSON = new File(configFile.toURI());
		TestConfig testConfig = new TestConfig(testConfigJSON);
		assertThat(testConfig.getFullConfig(), is(equalTo(String.format("{%1$s  \"resultFilesLocations\" : [],%1$s  \"resultsOutputIsCSVFormat\" : false,%1$s  \"someOtherElement\": \"foo\"%1$s}", 
				'\n'))));
	}

	@Test(expected = MojoExecutionException.class)
	public void testConfigFileDoesNotExist() throws MojoExecutionException {
		File testConfigJSON = new File("/does/not/exist");
		new TestConfig(testConfigJSON);
	}

	@Test
	public void createConfigFromInputStream() throws MojoExecutionException {
		InputStream configFile = this.getClass().getResourceAsStream(testConfigFile);
		TestConfig testConfig = new TestConfig(configFile);
		assertThat(testConfig.getFullConfig(), is(equalTo(String.format("{%1$s  \"resultFilesLocations\" : [],%1$s  \"resultsOutputIsCSVFormat\" : false,%1$s  \"someOtherElement\": \"foo\"%1$s}", 
				'\n'))));
	}

	@Test(expected = MojoExecutionException.class)
	public void testConfigResourceDoesNotExist() throws MojoExecutionException {
		InputStream configFile = this.getClass().getResourceAsStream("/does/not.exist");
		new TestConfig(configFile);
	}

	@Test
	public void changeCSVFormat() throws MojoExecutionException {
		InputStream configFile = this.getClass().getResourceAsStream(testConfigFile);
		TestConfig testConfig = new TestConfig(configFile);

		assertThat(testConfig.getResultsOutputIsCSVFormat(), is(equalTo(false)));

		testConfig.setResultsOutputIsCSVFormat(true);

		assertThat(testConfig.getResultsOutputIsCSVFormat(), is(equalTo(true)));

		testConfig.setResultsOutputIsCSVFormat(false);

		assertThat(testConfig.getResultsOutputIsCSVFormat(), is(equalTo(false)));
	}

	@Test
	public void changeResultsFileLocation() throws MojoExecutionException {
		InputStream configFile = this.getClass().getResourceAsStream(testConfigFile);
		TestConfig testConfig = new TestConfig(configFile);

		assertThat(testConfig.getResultsFileLocations().size(), is(equalTo(0)));

		List<String> resultFilenames = new ArrayList<>();
		resultFilenames.add(0, "c:\\windows\\temp");
		resultFilenames.add(1, "/usr/local/temp");

		testConfig.setResultsFileLocations(resultFilenames);

		assertThat(testConfig.getResultsFileLocations().size(), is(equalTo(2)));
		assertThat(testConfig.getResultsFileLocations().get(0), is(equalTo("c:\\windows\\temp")));
		assertThat(testConfig.getResultsFileLocations().get(1), is(equalTo("/usr/local/temp")));
	}

	@Test
	public void checkThatAWrittenFileCanBeReadInAgain() throws MojoExecutionException {

		String tempFileLocation = tempdir + File.separator + UUID.randomUUID() + File.separator + "test_config.json";
		File tempTestFile = new File(tempFileLocation);
		tempTestFile.getParentFile().mkdirs();
		tempTestFile.deleteOnExit();

		InputStream configFile = this.getClass().getResourceAsStream(testConfigFile);
		TestConfig testConfig = new TestConfig(configFile);
		testConfig.setResultsOutputIsCSVFormat(true);
		testConfig.writeResultFilesConfigTo(tempFileLocation);

		TestConfig newlyCreatedTestConfig = new TestConfig(tempTestFile);

		assertThat(testConfig, is(equalTo(newlyCreatedTestConfig)));
	}

	@Test(expected = MojoExecutionException.class)
	public void checkExceptionIsThrownIfFileCannotBeCreated() throws MojoExecutionException {

		String tempFileLocation = tempdir + File.separator + UUID.randomUUID() + File.separator + "test_config.json";
		File tempTestFile = new File(tempFileLocation);
		tempTestFile.deleteOnExit();

		InputStream configFile = this.getClass().getResourceAsStream(testConfigFile);
		TestConfig testConfig = new TestConfig(configFile);
		testConfig.setResultsOutputIsCSVFormat(true);
		testConfig.writeResultFilesConfigTo(tempFileLocation);
	}
}