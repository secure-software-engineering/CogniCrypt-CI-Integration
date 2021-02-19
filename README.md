# CogniCrypt for Build Environments

This repository hosts various plugins which integrate 
[CogniCrypt Static Analysis](https://www.eclipse.org/cognicrypt/)
into your build environments.

## Contents

Currently this repository lists the following plugins:

1. **Maven build plugin** - `maven`: Runs CogniCrypt analysis at the *verify* phase and reports any crypto API misuses.
Reports can be written to console or into a [SARIF (Static Analysis Results Interchange Format) file](http://docs.oasis-open.org/sarif/sarif/v2.0/csprd01/sarif-v2.0-csprd01.html).
2. **Jenkins Next Generation Warnings plugin** - `jenkins-ng-warnings`: As a post-build-action it takes a SARIF input and presents the findings.

Please see the individual folders for detailed information about building and installing.

## Contributing
TODO

## Limitations

##### Version 1.5-SNAPSHOT
- Maven Plugin only works for JCA CrySL ruleset.

## Credits

 

This work is part of the research project "AppSecure.nrw - Security-by-Design of Java-based Applications". The project is funded by the European Regional Development Fund (ERDF-0801379). 
<br/>
  <br/><img src="https://github.com/AppSecure-nrw/funding-notice/blob/main/Logo_48_lang_Zeichenfl%C3%A4che%201.png" alt="AppSecure.nrw Logo" height="50"/>
  
<img src="https://github.com/AppSecure-nrw/funding-notice/blob/main/EFRE_Foerderhinweis_englisch_farbig.jpg" alt="EFRE Logo" height="50"/> <img src="https://github.com/AppSecure-nrw/funding-notice/blob/main/Ziel2NRW_RGB_1809_jpg.jpg" alt="Ziel2NRW Logo" height="50"/>
