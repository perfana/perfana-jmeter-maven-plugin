JMeter Maven Plugin
=================================


[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/jmeter-maven-plugin/jmeter-maven-plugin.svg?branch=master)](https://travis-ci.org/jmeter-maven-plugin/jmeter-maven-plugin)
[![codecov](https://codecov.io/gh/jmeter-maven-plugin/jmeter-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/jmeter-maven-plugin/jmeter-maven-plugin)

[![GitHub release](https://img.shields.io/github/release/jmeter-maven-plugin/jmeter-maven-plugin.svg?colorB=brightgreen)](http://jmeter.lazerycode.com/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.lazerycode.jmeter/jmeter-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.lazerycode.jmeter/jmeter-maven-plugin)
[![Javadocs](https://www.javadoc.io/badge/com.lazerycode.jmeter/jmeter-maven-plugin.svg)](https://www.javadoc.io/doc/com.lazerycode.jmeter/jmeter-maven-plugin)
[![JitPack](https://jitpack.io/v/jmeter-maven-plugin/jmeter-maven-plugin.svg)](https://jitpack.io/#jmeter-maven-plugin/jmeter-maven-plugin)

[![Join the chat at https://gitter.im/jmeter-maven-plugin/jmeter-maven-plugin](https://badges.gitter.im/jmeter-maven-plugin/jmeter-maven-plugin.svg)](https://gitter.im/jmeter-maven-plugin/jmeter-maven-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Stack Overflow](https://img.shields.io/:stack%20overflow-jmeter_maven_plugin-brightgreen.svg)](https://stackoverflow.com/questions/tagged/jmeter-maven-plugin)
[![Twitter](https://img.shields.io/twitter/url/https/github.com/jmeter-maven-plugin/jmeter-maven-plugin.svg?style=social)](https://twitter.com/intent/tweet?text=Integrate+easily+%40ApacheJMeter+in+your+%23Maven+project+with+jmeter-maven-plugin:&url=https%3A%2F%2Fgithub.com%2Fjmeter-maven-plugin%2Fjmeter-maven-plugin)

A Maven plugin that provides the ability to run JMeter tests as part of your build

See the [CHANGELOG](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/blob/master/CHANGELOG.md) for change information.  

All the documentation you need to configure the plugin is available on the [github wiki](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/wiki).

Last version is **2.8.0** requires Maven >= 3.5.0 and is compatible with **Apache JMeter 5.0**.

This plugin requires a JDK between **8** and **10**.  Java 11 will not work due to this Java [bug](https://bugs.openjdk.java.net/browse/JDK-8210005).

See this for a [possible workaround](https://stackoverflow.com/a/52510406/460802) on Java 11. 

Basic Usage
-----

### Add the plugin to your project

Add the plugin to the build section of your pom's project :

```
<plugin>
    <groupId>io.perfana</groupId>
    <artifactId>perfana-jmeter-maven-plugin</artifactId>
    <version>2.8.0</version>
    <executions>
	   <!-- Run JMeter tests -->
       <execution>
            <id>jmeter-tests</id>
            <goals>
                <goal>jmeter</goal>
            </goals>
       </execution>
       <!-- Fail build on errors in test -->
       <execution>
            <id>jmeter-check-results</id>
            <goals>
                <goal>results</goal>
            </goals>
       </execution>
    </executions>
</plugin>
```

### Reference JMX files and CSV data

Once you have created your JMeter tests, you'll need to copy them to `<Project Dir>/src/test/jmeter`.  By default this plugin will pick up all the .jmx files in that directory, to specify which tests should be run please see the project documentation. 

You can also put data files in this folder and reference them in your plan.

### Run the tests

```mvn verify```

All your tests will run in maven!

Documentation
-----

All the documentation you need to configure the plugin is available on the [github wiki](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/wiki).

Beginners should start with the [Basic Configuration](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/wiki/Basic-Configuration) section.

For advanced POM configuration settings have a look at the [Advanced Configuration](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/wiki/Advanced-Configuration) section.

Books
-----

If you'd like to thank the maintainer of this project and some developers of Apache JMeter, you can buy this book:

[![Master JMeter book logo](https://raw.githubusercontent.com/jmeter-maven-plugin/jmeter-maven-plugin/master/master-jmeter-from-load-test-to-devops-medium.png)](https://leanpub.com/master-jmeter-from-load-test-to-devops/)

Community
-----

### Users Group

A place to discuss usage of the maven-jmeter-plugin, let people know how you use it here.

Homepage: [http://groups.google.com/group/maven-jmeter-plugin-users](http://groups.google.com/group/maven-jmeter-plugin-users)

Group Email: [maven-jmeter-plugin-users@googlegroups.com](mailto:maven-jmeter-plugin-users@googlegroups.com)

### Devs Group

A place to discuss the development of the maven-jmeter-plugin, or ask about features you would like to see added.

Homepage: [http://groups.google.com/group/maven-jmeter-plugin-devs]( http://groups.google.com/group/maven-jmeter-plugin-devs)

Group Email: [maven-jmeter-plugin-devs@googlegroups.com](mailto:maven-jmeter-plugin-devs@googlegroups.com)

### Website

The official website is available at [http://jmeter.lazerycode.com](http://jmeter.lazerycode.com)

We love it when people [Contribute](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/blob/master/CONTRIBUTING.md)!