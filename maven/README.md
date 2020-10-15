#CogniCrypt Maven Plugin	

This is a maven plugin that runs CogniCrypt on a maven project.

## Installing the Plugin

### Building from Source

1. Clone this repository.
2. Install the plugin by running: `mvn clean install` within this folder.

### Retrieving from Server
*Not yet supported!*


## Using the Plugin

Make sure the plugin is installed!

### Standalone Execution

Because CogniCrypt requires some resources, especially when running on larger project, 
we recommend setting the `MAVEN_OPTS` environment variable with:
`export MAVEN_OPTS="-Xmx8g -Xss100m"`

**Running the plugin:**

`mvn de.fraunhofer.iem:cognicrypt-maven:cognicrypt -Dcognicrypt.rulesDirectory=<path-to-cryptslbins> -Dcognicrypt.outputFormat=sarif`
 
 The analysis writes its results either in SARIF-format or Standard CogniCryput output to a file in target/cognicrypt-reports
 
 Note: In the case the maven project is a multi-module project, the output is generated for each module.
 
 ### Running as Build Plugin
 
 1. Add the following snipped to your project's `pom.xml`
 2. Run `mvn clean verify` on your project.
 
 
*The pom.xml snippet:*
```xml
 <build>
    <plugins>
      <plugin>
        <groupId>de.fraunhofer.iem</groupId>
        <artifactId>cognicrypt-maven</artifactId>
        <version>1.5-SNAPSHOT</version>
        <executions>
            <execution>
              <id>run-cognicrypt</id>
              <phase>verify</phase>
              <goals><goal>cognicrypt</goal></goals>
              <configuration>
                <rulesDirectory>PATH_TO_CRYSL</rulesDirectory>
                <outputFormat>sarif</outputFormat>
              </configuration>
            </execution>
          </executions>
      </plugin>
    </plugins>
</build>
```

 
 *See the `..\testApplication\simpleTestProgram\pom.xml` for an example. 
 It also includes how to do automatic CrySL ruleset donwload.*
 
 ## CogniCrypt Maven Plugin Options
 
The maven plugin can be used with different options (speficied as tags in XML)

`rulesDirectory`:
- Absolute or relative path to a directory which contains `.crysl` rules.

`callGraph`: 
- cha (default): Uses a Class Hierarchy Analysis as call graph.
- spark: Uses a points-to based call graph (might be unsound for incomplete programs)

`dynamic-cg`:
- true (default): Generates a partial call graph on-the-fly during data-flow propagation for each individual object. 
This options increases runtime performence.
- false 

`includeDependencies`: 
- false (default): Exclude maven dependencies when building the call graph. 
Setting this option to false when analyzing with any other ruleset then the JCA one will lead to unsound results.
- true: Include the maven dependencies when building the call graph.

`outputFormat`: 
- standard (default) Reports as text to the command line.
- sarif Generates a report in SARIF format (CogniCrypt-SARIF-Report.json).

`reportsDirectory`:
- The directory the output files are put into.

`excludedPackages`:
- List of package prefixes that are put excluded during the data-flow analysis (no bodies are loaded for these methods).
The list is supplied as String, each element is separated by a comma. 
Default value is: `"java.*,javax.*,sun.*,com.sun.*,com.ibm.*,org.xml.*,org.w3c.*,apple.awt.*,com.apple.*"` which excludes the JRE.