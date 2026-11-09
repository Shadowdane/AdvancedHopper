
$env:JAVA_HOME = "F:\Java\jdk-25.0.4.1+1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

.\gradlew.bat :advancedhopper-fabric:build
pause

