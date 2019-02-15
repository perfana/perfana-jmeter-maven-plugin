package com.lazerycode.jmeter.mojo;

import com.lazerycode.jmeter.configuration.JMeterArgumentsArray;
import com.lazerycode.jmeter.configuration.JMeterProcessJVMSettings;
import com.lazerycode.jmeter.configuration.ProxyConfiguration;
import com.lazerycode.jmeter.configuration.RemoteConfiguration;
import com.lazerycode.jmeter.exceptions.IOException;
import io.perfana.client.PerfanaClient;
import io.perfana.client.PerfanaClientBuilder;
import io.perfana.client.api.PerfanaClientLogger;
import io.perfana.client.api.PerfanaConnectionSettings;
import io.perfana.client.api.PerfanaConnectionSettingsBuilder;
import io.perfana.client.api.TestContext;
import io.perfana.client.api.TestContextBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.lazerycode.jmeter.utility.UtilityFunctions.isSet;

/**
 * JMeter Maven plugin.
 * This is a base class for the JMeter mojos.
 *
 * @author Tim McCune
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal", "JavaDoc"}) // Mojos get their fields set via reflection
public abstract class AbstractJMeterMojo extends AbstractMojo {
    protected static final String LINE_SEPARATOR = "-------------------------------------------------------";

    protected static final String JMETER_ARTIFACT_PREFIX = "ApacheJMeter_";
    protected static final String JMETER_ARTIFACT_NAME = "ApacheJMeter";
    protected static final String JMETER_CONFIG_ARTIFACT_NAME = "ApacheJMeter_config";
    protected static final String JORPHAN_ARTIFACT_NAME = "jorphan";
    protected static final String JMETER_GROUP_ID = "org.apache.jmeter";

	/**
	 * Sets the list of include patterns to use in directory scan for JMX files.
	 * Relative to testFilesDirectory.
	 */
	@Parameter
	protected List<String> testFilesIncluded = new ArrayList<>();

	/**
	 * Sets the list of exclude patterns to use in directory scan for JMX files.
	 * Relative to testFilesDirectory.
	 */
	@Parameter
	protected List<String> testFilesExcluded = new ArrayList<>();

	/**
	 * Path under which .conf files are stored.
	 */
	@Parameter(defaultValue = "${basedir}/src/test/conf")
	protected File confFilesDirectory;

	/**
	 * Path under which JMX files are stored.
	 */
	@Parameter(defaultValue = "${basedir}/src/test/jmeter")
	protected File testFilesDirectory;

	/**
	 * Timestamp the test results.
	 */
	@Parameter(defaultValue = "true")
	protected boolean testResultsTimestamp;

	/**
	 * Append the results timestamp to the filename
	 * (It will be prepended by default if testResultsTimestamp is set to true)
	 */
	@Parameter(defaultValue = "false")
	protected boolean appendResultsTimestamp;

	/**
	 * Set the format of the timestamp that is appended to the results filename.
	 * (This assumes that testResultsTimestamp is set to 'true')
	 * For formatting see http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html
	 */
	@Parameter()
	protected String resultsFileNameDateFormat;

	/**
	 * Set the directory that JMeter results are saved to.
	 */
	@Parameter(defaultValue = "${project.build.directory}/jmeter/results")
	protected File resultsDirectory;

	/**
	 * Generate JMeter Reports (this will force your .jtl's into .csv mode)
	 */
	@Parameter(defaultValue = "true")
	protected boolean generateReports;

	/**
	 * Set the directory that JMeter reports are saved to.
	 */
	@Parameter(defaultValue = "${project.build.directory}/jmeter/reports")
	protected File reportDirectory;

	/**
	 * Set the directory that JMeter test files are copied into as part of the build.
	 */
	@Parameter(defaultValue = "${project.build.directory}/jmeter/testFiles")
	protected File testFilesBuildDirectory;

	/**
	 * Set the directory that JMeter logs are saved to.
	 */
	@Parameter(defaultValue = "${project.build.directory}/jmeter/logs")
	protected File logsDirectory;

	/**
	 * Absolute path to JMeter custom (test dependent) properties file.
	 */
	@Parameter
	protected List<File> customPropertiesFiles = new ArrayList<>();

	/**
	 * Use maven proxy configuration if no specific proxy configuration provided
	 */
	@Parameter
	protected boolean useMavenProxy;

	/**
	 * Maven settings
	 */
	@Parameter(defaultValue = "${settings}", readonly = true)
	protected Settings settings;


	/**
	 * Value class that wraps all proxy configurations.
	 */
	@Parameter
	protected ProxyConfiguration proxyConfig;

	/**
	 * Value class that wraps all remote configurations.
	 */
	@Parameter
	protected RemoteConfiguration remoteConfig;

	/**
	 * Value class that wraps all JMeter Process JVM settings.
	 */
	@Parameter
	protected JMeterProcessJVMSettings jMeterProcessJVMSettings;

	/**
	 * Set a root log level to override all log levels used by JMeter
	 * Valid log levels are: ERROR, WARN, INFO, DEBUG (They are not case sensitive);
	 * If you try to set an invalid log level it will be ignored
	 */
	@Parameter
	protected String overrideRootLogLevel;

	/**
	 * Suppress JMeter output
	 */
	@Parameter(defaultValue = "false")
	protected boolean suppressJMeterOutput;

	/**
	 * The information extracted from the Mojo being currently executed
	 */
	@Parameter(defaultValue = "${mojoExecution}", required = true, readonly = true)
	protected MojoExecution mojoExecution;

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;

	/**
	 * Skip the JMeter tests
	 */
	@Parameter(defaultValue = "${skipTests}")
	protected boolean skipTests;

	/**
	 * Set a pause in seconds after each test that is run.
	 */
	@Parameter(defaultValue = "0")
	protected String postTestPauseInSeconds;

	/**
	 * The filename used to store the results config
	 */
	@Parameter(defaultValue = "${project.build.directory}/config.json")
	protected String testConfigFile;

	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Place where the JMeter files will be generated.
	 */
	@Parameter(defaultValue = "${project.build.directory}/jmeter")
	protected File jmeterDirectory;

	/**
	 * The project build directory
	 */
	@Parameter(defaultValue = "${project.build.directory}")
	protected File projectBuildDirectory;

	//==================================================================================================================

    /**
     * Perfana: Name of application that is being tested.
     */
    @Parameter(defaultValue = "UNKNOWN_APPLICATION")
    protected String perfanaApplication;

    /**
     * Perfana: Test type for this test.
     */
    @Parameter(defaultValue = "UNKNOWN_TEST_TYPE")
    protected String perfanaTestType;

    /**
     * Perfana: Test environment for this test.
     */
    @Parameter(defaultValue = "UNKNOWN_TEST_ENVIRONMENT")
    protected String perfanaTestEnvironment;

    /**
     * Perfana: Test run id.
     */
    @Parameter(defaultValue = "UNKNOWN_TEST_RUN_ID")
    protected String perfanaTestRunId;

    /**
     * Perfana: Build results url where to find the results of this load test.
     */
    @Parameter
    protected String perfanaCIBuildResultsUrl;

    /**
     * Perfana: Perfana url.
     */
    @Parameter(defaultValue = "UNKNOWN_PERFANA_URL")
    protected String perfanaUrl;

    /**
     * Perfana: the release number of the application.
     */
    @Parameter(defaultValue = "UNKNOWN_APPLICATION_RELEASE")
    protected String perfanaApplicationRelease;

    /**
     * Perfana: Rampup time in seconds.
     */
    @Parameter(defaultValue = "0")
    protected String perfanaRampupTimeInSeconds;

    /**
     * Perfana: Constant load time in seconds.
     */
    @Parameter(defaultValue = "120")
    protected String perfanaConstantLoadTimeInSeconds;

    /**
     * Perfana: Parse the Perfana test asserts and fail build it not ok.
     */
    @Parameter(defaultValue = "false")
    protected boolean perfanaAssertResultsEnabled;

    /**
     * Perfana: Enable calls to Perfana.
     */
    @Parameter(defaultValue = "false")
    protected boolean perfanaEnabled;

    /**
     * Perfana: test run annotiations passed via environment variable
     */
    @Parameter
    protected String perfanaAnnotations;

    /**
     * Perfana: test run variables passed via environment variable
     */
    @Parameter
    protected Map<String,String> perfanaVariables;

    /**
     * Perfana: properties for perfana event implementations
     */
    @Parameter
    protected Map<String, Properties> perfanaEventProperties;

    /**
     * Perfana: schedule script with events, one event per line, such as: PT1M|scale-down|replicas=2
     */
    @Parameter
    protected String eventScheduleScript;

    //==================================================================================================================

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		if (skipTests) {
			if (session.getGoals().contains("jmeter:gui")) {
				if (!"default-cli".equals(mojoExecution.getExecutionId()) && 
				        !"compile".equals(mojoExecution.getLifecyclePhase())) {
					getLog().info("Performance tests are skipped.");
					return;
				}
			} else {
				getLog().info("Performance tests are skipped.");
				return;
			}
		}

		// load maven proxy if needed
		if (useMavenProxy && proxyConfig == null) {
			loadMavenProxy();
		}
		
        doExecute();
        
    }

	protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

	/**
	 * Generate the initial JMeter Arguments array that is used to create the command line that we pass to JMeter.
	 *
	 * @throws MojoExecutionException
	 */
	protected JMeterArgumentsArray computeJMeterArgumentsArray(boolean disableGUI, boolean isCSVFormat) throws MojoExecutionException {
	    JMeterArgumentsArray testArgs = new JMeterArgumentsArray(disableGUI, jmeterDirectory.getAbsolutePath());
		testArgs.setResultsDirectory(resultsDirectory.getAbsolutePath());
		testArgs.setResultFileOutputFormatIsCSV(isCSVFormat);
		if (generateReports && disableGUI) {
			testArgs.setReportsDirectory(reportDirectory.getAbsolutePath());
		}
		if (testResultsTimestamp) {
			testArgs.setResultsTimestamp(true);
			testArgs.appendTimestamp(appendResultsTimestamp);
			if (isSet(resultsFileNameDateFormat)) {
				try {
					testArgs.setResultsFileNameDateFormat(DateTimeFormat.forPattern(resultsFileNameDateFormat));
				} catch (Exception ex) {
					getLog().error("'" + resultsFileNameDateFormat + "' is an invalid DateTimeFormat.  Defaulting to Standard ISO_8601.", ex);
				}
			}
		}
		testArgs.setProxyConfig(proxyConfig);
		for (File customPropertiesFile : customPropertiesFiles) {
			testArgs.setACustomPropertiesFile(customPropertiesFile);
		}
		testArgs.setLogRootOverride(overrideRootLogLevel);
		testArgs.setLogsDirectory(logsDirectory.getAbsolutePath());
		return testArgs;
	}

	/**
	 * Try to load the active maven proxy.
	 */
	protected void loadMavenProxy() {
		if (settings == null)
			return;

		try {
			Proxy mvnProxy = settings.getActiveProxy();

			if (mvnProxy != null) {

				ProxyConfiguration newProxyConf = new ProxyConfiguration();
				newProxyConf.setHost(mvnProxy.getHost());
				newProxyConf.setPort(mvnProxy.getPort());
				newProxyConf.setUsername(mvnProxy.getUsername());
				newProxyConf.setPassword(mvnProxy.getPassword());
				newProxyConf.setHostExclusions(mvnProxy.getNonProxyHosts());
				proxyConfig = newProxyConf;

				getLog().info("Maven proxy loaded successfully");
			} else {
				getLog().warn("No maven proxy found, but useMavenProxy set to true.");
			}
		} catch (Exception e) {
			getLog().error("Error while loading maven proxy", e);
		}
	}

	static void copyFilesInTestDirectory(File sourceDirectory, File destinationDirectory) throws IOException { // NOSONAR
		try {
			FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
		} catch (java.io.IOException e) {
			throw new IOException(e.getMessage(), e);
		}

	}

    PerfanaClient createPerfanaClient() {
        final PerfanaClientLogger logger = new PerfanaClientLogger() {
            @Override
            public void info(String message) {
                getLog().info(message);
            }

            @Override
            public void warn(String message) {
                getLog().warn(message);
            }

            @Override
            public void error(String message) {
                getLog().error(message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                getLog().error(message, throwable);
            }

            @Override
            public void debug(final String message) {
                getLog().debug(message);
            }
        };

        TestContext context = new TestContextBuilder()
                .setTestRunId(perfanaTestRunId)
                .setApplication(perfanaApplication)
                .setTestType(perfanaTestType)
                .setTestEnvironment(perfanaTestEnvironment)
                .setTestRunId(perfanaTestRunId)
                .setCIBuildResultsUrl(perfanaCIBuildResultsUrl)
                .setApplicationRelease(perfanaApplicationRelease)
                .setRampupTimeInSeconds(perfanaRampupTimeInSeconds)
                .setConstantLoadTimeInSeconds(perfanaConstantLoadTimeInSeconds)
                .setVariables(perfanaVariables)
                .setAnnotations(perfanaAnnotations)
                .build();

        PerfanaConnectionSettings settings = new PerfanaConnectionSettingsBuilder()
                .setPerfanaUrl(perfanaUrl)
                .build();

        PerfanaClientBuilder clientBuilder = new PerfanaClientBuilder()
                .setLogger(logger)
                .setTestContext(context)
                .setPerfanaConnectionSettings(settings)
                .setAssertResultsEnabled(perfanaAssertResultsEnabled)
                .setCustomEvents(eventScheduleScript);

        if (perfanaEventProperties != null) {
            perfanaEventProperties.forEach(
                    (className, props) -> props.forEach(
                            (name, value) -> clientBuilder.addEventProperty(className, (String) name, (String) value)));
        }
        
        return clientBuilder.build();
    }
    
}