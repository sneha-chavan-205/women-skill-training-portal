pipeline {
    agent any

    environment {
        CHROMEDRIVER_PATH = 'C:\\Tools\\ChromeDriver\\chromedriver.exe'
    }

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
                    if not defined CHROMEDRIVER_PATH (
                        echo ERROR: Configure CHROMEDRIVER_PATH for the Jenkins agent.
                        exit /b 1
                    )
                    if not exist "%CHROMEDRIVER_PATH%" (
                        echo ERROR: ChromeDriver was not found at CHROMEDRIVER_PATH.
                        exit /b 1
                    )
                    if not exist "%MAVEN_HOME%\\bin\\mvn.cmd" (
                        echo ERROR: mvn.cmd was not found under MAVEN_HOME.
                        exit /b 1
                    )
                    "%CHROMEDRIVER_PATH%" --version
                    call "%MAVEN_HOME%\\bin\\mvn.cmd" -version
                    call "%MAVEN_HOME%\\bin\\mvn.cmd" -B clean verify
                '''
            }
        }

        stage('Docker image and local deployment') {
            steps {
                script {
                    def dockerExe = 'C:\\Users\\hp\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
                    def composeExe = 'C:\\Users\\hp\\.docker\\cli-plugins\\docker-compose.exe'
                    def dockerStatus = bat(returnStatus: true, script: """
                        @echo off
                        echo Docker CLI location:
                        echo ${dockerExe}
                        if not exist "${dockerExe}" (
                            echo ERROR: Docker CLI was not found at the configured absolute path.
                            exit /b 1
                        )
                        echo Docker CLI context:
                        "${dockerExe}" context show
                        if errorlevel 1 (
                            echo ERROR: Docker CLI could not read its active context.
                            exit /b 1
                        )
                        echo Checking Docker client and engine:
                        "${dockerExe}" version
                        if errorlevel 1 (
                            echo ERROR: Docker CLI is present, but the Docker engine is unavailable to this Jenkins agent.
                            exit /b 1
                        )
                    """)
                    if (dockerStatus != 0) {
                        error('Docker is required for this pipeline deployment stage; see the Docker CLI/context diagnostics above.')
                    } else {
                        def composeStatus = bat(returnStatus: true, script: """
                            @echo off
                            echo Docker Compose executable:
                            echo ${composeExe}
                            if not exist "${composeExe}" (
                                echo ERROR: Docker Compose executable was not found at the configured absolute path.
                                exit /b 1
                            )
                            echo Docker Compose version:
                            "${composeExe}" version
                            if errorlevel 1 (
                                echo ERROR: Docker Compose executable could not report its version.
                                exit /b 1
                            )
                        """)
                        if (composeStatus != 0) {
                            error('Docker Compose is required for this pipeline deployment stage; check the executable path and version output above.')
                        }

                        def imageTag = "women-skill-training-portal:${env.BUILD_NUMBER}"
                        def composeProject = "wstp-jenkins-${env.BUILD_NUMBER}"
                        withEnv([
                            "APP_IMAGE=${imageTag}",
                            "APP_PORT=0",
                            "COMPOSE_PROJECT_NAME=${composeProject}",
                            "MYSQL_DATABASE=wstp_ci_${env.BUILD_NUMBER}",
                            "MYSQL_USER=wstp_ci",
                            "MYSQL_PASSWORD=ci-${env.BUILD_TAG}",
                            "MYSQL_ROOT_PASSWORD=ci-root-${env.BUILD_TAG}"
                        ]) {
                            bat """
                                "${dockerExe}" build --tag "%APP_IMAGE%" .
                                if errorlevel 1 exit /b 1
                                echo ==== Diagnostic: docker info ====
                                "${dockerExe}" info
                                set "DOCKER_INFO_EXIT=%ERRORLEVEL%"
                                if not "%DOCKER_INFO_EXIT%"=="0" echo ERROR: docker info exited with code %DOCKER_INFO_EXIT%.
                                echo ==== Diagnostic: Docker Compose up --help ====
                                "${composeExe}" up --help
                                set "COMPOSE_UP_HELP_EXIT=%ERRORLEVEL%"
                                if not "%COMPOSE_UP_HELP_EXIT%"=="0" echo ERROR: Docker Compose up --help exited with code %COMPOSE_UP_HELP_EXIT%.
                                "${composeExe}" up --detach --no-build
                                if errorlevel 1 exit /b 1
                            """

                            def appReady = false
                            def lastLogExitCode = 0
                            for (int attempt = 1; attempt <= 30; attempt++) {
                                def pollOutput = bat(returnStdout: true, script: """
                                    @echo off
                                    "${composeExe}" logs --no-color --tail 200 app 2>&1
                                    set "WSTP_COMPOSE_LOG_EXIT=%ERRORLEVEL%"
                                    echo __WSTP_COMPOSE_LOG_EXIT__=%WSTP_COMPOSE_LOG_EXIT%
                                    exit /b 0
                                """).trim()

                                def exitMarker = '__WSTP_COMPOSE_LOG_EXIT__='
                                def exitMarkerIndex = pollOutput.lastIndexOf(exitMarker)
                                def appLogs = exitMarkerIndex >= 0
                                    ? pollOutput.substring(0, exitMarkerIndex).trim()
                                    : pollOutput
                                lastLogExitCode = 1
                                if (exitMarkerIndex >= 0) {
                                    def exitCodeText = pollOutput.substring(exitMarkerIndex + exitMarker.length()).trim()
                                    try {
                                        lastLogExitCode = Integer.parseInt(exitCodeText)
                                    } catch (NumberFormatException ignored) {
                                        lastLogExitCode = 1
                                    }
                                }

                                if (lastLogExitCode == 0 && appLogs.contains('Started WomenSkillTrainingPortalApplication')) {
                                    appReady = true
                                    break
                                }
                                if (attempt < 30) {
                                    sleep(time: 2, unit: 'SECONDS')
                                }
                            }

                            if (!appReady) {
                                echo("Application readiness check failed after 30 polls; last Compose app-log exit code: ${lastLogExitCode}.")
                                bat(returnStatus: true, script: """
                                    @echo off
                                    echo ==== Compose service and container status ====
                                    "${composeExe}" ps --all
                                    echo ==== Application logs ====
                                    "${composeExe}" logs --no-color --tail 200 app
                                    echo ==== Database logs ====
                                    "${composeExe}" logs --no-color --tail 200 db
                                    exit /b 0
                                """)
                                error('WSTP application did not report successful startup within approximately 60 seconds.')
                            }
                            echo('WSTP application reported successful startup.')
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
        }
    }
}
