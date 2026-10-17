pipeline {
    agent any

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Verify Java 21') {
            steps {
                bat '''
                    if not defined JAVA_HOME (
                        echo ERROR: Configure JAVA_HOME for the Jenkins agent.
                        exit /b 1
                    )
                    if not exist "%JAVA_HOME%\\bin\\java.exe" (
                        echo ERROR: java.exe was not found under JAVA_HOME.
                        exit /b 1
                    )
                    "%JAVA_HOME%\\bin\\java.exe" -version 2>&1 | findstr /C:"21." >nul
                    if errorlevel 1 (
                        echo ERROR: Jenkins agent JAVA_HOME must point to Java 21.
                        exit /b 1
                    )
                    "%JAVA_HOME%\\bin\\java.exe" -version
                '''
            }
        }

        stage('Build and test') {
            steps {
                bat '''
                    if not defined MAVEN_HOME (
                        echo ERROR: Configure MAVEN_HOME for the Jenkins agent.
                        exit /b 1
                    )
                    if not defined MAVEN_OPTS (
                        echo ERROR: Configure MAVEN_OPTS to use the approved Maven local repository.
                        exit /b 1
                    )
                    if not exist "%MAVEN_HOME%\\bin\\mvn.cmd" (
                        echo ERROR: mvn.cmd was not found under MAVEN_HOME.
                        exit /b 1
                    )
                    call "%MAVEN_HOME%\\bin\\mvn.cmd" -version
                    call "%MAVEN_HOME%\\bin\\mvn.cmd" -B clean verify
                '''
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
        }
    }
}
