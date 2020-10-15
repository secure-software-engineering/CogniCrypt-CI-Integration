#CogniCrypt Maven Plugin	

The Jenkins plugin searches the repositories folders for results a file containing results in the [SARIF format](http://docs.oasis-open.org/sarif/sarif/v2.0/csprd01/sarif-v2.0-csprd01.html). 
These results are produced by the maven plugin. 

## Building

1. Clone this repository.
2. Install the plugin by running: `mvn clean install` within this folder.

## Installing to Jenkins

The plugin requires some global setup on the Jenkins Server:

1. Increase the memory in Jenkins' Maven by setting `MAVEN_OPTS="-Xmx8g -Xss100m"` in Jenkins 
(`Jenkins > Manage Jenkins > Configure System > Global MAVEN_OPTS`).
2. Download the latest CrySL rules from [the CrySL Ruleset Repo](https://github.com/CROSSINGTUD/Crypto-API-Rules).
Pick the `.crysl` files from a `src` folder and place them into a new directry of your choice (hereafter referred to by PATH_TO_CRYSL). 
3. Make this folder accessible from Jenkins.

Next prepare your project so it uses a CogniCrypt build plugin (e.g. Maven Build Plugin).
Make sure to set the `outputDirectory` option to `sarif`.

Setup your Build Pipeline as follows:
1. Add a `Post-Build-Action` named `Record compiler warnings and static analysis results`. 
2. Select `CogniCrypt Parser` as Tool.
3. Set `Report File Pattern` to `**\CryptoAnalysis-Report.json`
4. Save
5. After each build, the analysis should report the CogniCrypt warnings in the panel "CogniCrypt Parser Warnings"
