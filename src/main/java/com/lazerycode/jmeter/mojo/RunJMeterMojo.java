package com.lazerycode.jmeter.mojo;

import com.lazerycode.jmeter.configuration.JMeterArgumentsArray;
import com.lazerycode.jmeter.json.TestConfig;
import com.lazerycode.jmeter.testrunner.TestManager;
import io.perfana.client.PerfanaClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Goal that runs jmeter based on configuration defined in your pom.<br/>
 * This goal runs within Lifecycle phase {@link LifecyclePhase#INTEGRATION_TEST}.
 */
@Mojo(name = "jmeter", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
@Execute(goal = "configure")
public class RunJMeterMojo extends AbstractJMeterMojo {

	/**
	 * Run all the JMeter tests.
	 *
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Override
	public void doExecute() throws MojoExecutionException, MojoFailureException {
		getLog().info(" ");
		getLog().info(LINE_SEPARATOR);
		getLog().info(" P E R F O R M A N C E    T E S T S");
		getLog().info(LINE_SEPARATOR);

		if (!testFilesDirectory.exists()) {
			getLog().info("<testFilesDirectory>" + testFilesDirectory.getAbsolutePath() + "</testFilesDirectory> does not exist...");
			getLog().info("Performance tests are skipped.");
			return;
		}

		TestConfig testConfig = new TestConfig(new File(testConfigFile));
		JMeterArgumentsArray testArgs = computeJMeterArgumentsArray(true, testConfig.getResultsOutputIsCSVFormat());

		if (null != remoteConfig) {
			remoteConfig.setPropertiesMap(JMeterConfigurationHolder.getInstance().getPropertiesMap());
		}

		copyFilesInTestDirectory(testFilesDirectory, testFilesBuildDirectory);

        final ScheduledExecutorService exec;
        final PerfanaClient perfanaClient = perfanaEnabled
                ? createPerfanaClient()
                : null;
        
        TestManager jMeterTestManager =
		        new TestManager(testArgs, testFilesBuildDirectory, testFilesIncluded, testFilesExcluded, 
		                remoteConfig, suppressJMeterOutput, JMeterConfigurationHolder.getInstance().getWorkingDirectory(), jMeterProcessJVMSettings, 
		                JMeterConfigurationHolder.getInstance().getRuntimeJarName(), reportDirectory, generateReports, perfanaClient);
		jMeterTestManager.setPostTestPauseInSeconds(postTestPauseInSeconds);
		getLog().info(" ");
		if (proxyConfig != null) {
			getLog().info(this.proxyConfig.toString());
		}

		testConfig.setResultsFileLocations(jMeterTestManager.executeTests());
		testConfig.writeResultFilesConfigTo(testConfigFile);
	}
}