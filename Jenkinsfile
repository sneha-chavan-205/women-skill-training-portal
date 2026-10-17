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
                    def dockerStatus = bat(returnStatus: true, script: 'docker info >NUL 2>&1')
                    if (dockerStatus != 0) {
                        echo 'Docker engine is not accessible to this Jenkins agent; skipping image build and local deployment.'
                    } else {
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
                            bat '''
                                docker build --tag "%APP_IMAGE%" .
                                if errorlevel 1 exit /b 1
                                docker compose -p "%COMPOSE_PROJECT_NAME%" up --detach --no-build
                                if errorlevel 1 exit /b 1
                                for /L %%i in (1,1,30) do (
                                    docker compose -p "%COMPOSE_PROJECT_NAME%" logs --no-color app | findstr /C:"Started WomenSkillTrainingPortalApplication" >NUL
                                    if not errorlevel 1 exit /b 0
                                    timeout /t 2 /nobreak >NUL
                                )
                                docker compose -p "%COMPOSE_PROJECT_NAME%" logs --no-color app
                                exit /b 1
                            '''
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
