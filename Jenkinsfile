pipeline {

    agent any

    environment {
        APP_PORT = '8081'
        APP_URL = 'http://localhost:8081'
        DEPLOY_DIR = 'C:\\WSTP-Deployment'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    stages {

        // =========================================================
        // 1. CHECKOUT
        // =========================================================

        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }


        // =========================================================
        // 2. CHECKOUT INFORMATION
        // =========================================================

        stage('Checkout') {
            steps {
                bat '''
                    echo ========================================
                    echo CHECKING SOURCE CODE
                    echo ========================================

                    git branch --show-current
                    git log -1 --oneline

                    echo.
                    echo Source code checkout completed.
                '''
            }
        }


        // =========================================================
        // 3. BUILD
        // =========================================================

        stage('Build') {
            steps {
                bat '''
                    echo ========================================
                    echo BUILDING APPLICATION
                    echo ========================================

                    mvn -B clean compile

                    if %ERRORLEVEL% NEQ 0 (
                        echo BUILD FAILED
                        exit /b 1
                    )

                    echo.
                    echo BUILD SUCCESSFUL
                '''
            }
        }


        // =========================================================
        // 4. CONTINUOUS TESTING
        // =========================================================

        stage('Test') {
    steps {
        echo '=========================================='
        echo 'TEST'
        echo '=========================================='

        echo 'Running Spring Boot application tests...'

        withCredentials([
            string(
                credentialsId: 'mysql-db-password',
                variable: 'DB_PASSWORD'
            )
        ]) {

            bat '''
                echo ==========================================
                echo DATABASE CONFIGURATION
                echo ==========================================
                echo MySQL Host: localhost
                echo MySQL Port: 3306
                echo Database: women_skill_training_portal
                echo DB_PASSWORD: CONFIGURED
                echo ==========================================

                echo.
                echo Running Maven tests...
                echo.

                mvn test
            '''
        }
    }

    post {
        always {
            echo 'Publishing JUnit test reports...'

            junit(
                testResults: 'target/surefire-reports/*.xml',
                allowEmptyResults: false
            )
        }

        success {
            echo '=========================================='
            echo 'ALL TESTS PASSED'
            echo 'DEPLOYMENT MAY CONTINUE'
            echo '=========================================='
        }

        failure {
            echo '=========================================='
            echo 'TESTS FAILED'
            echo 'DEPLOYMENT BLOCKED'
            echo '=========================================='
        }
    }
}

        // =========================================================
        // 5. PACKAGE
        // =========================================================

        stage('Package') {
            steps {
                bat '''
                    echo ========================================
                    echo PACKAGING SPRING BOOT APPLICATION
                    echo ========================================

                    mvn -B package -DskipTests

                    if %ERRORLEVEL% NEQ 0 (
                        echo PACKAGE FAILED
                        exit /b 1
                    )

                    echo.
                    echo PACKAGE SUCCESSFUL

                    dir target\\*.jar
                '''
            }
        }


        // =========================================================
        // 6. ARCHIVE ARTIFACT
        // =========================================================

        stage('Archive Artifact') {
            steps {

                echo '========================================'
                echo 'ARCHIVING JAR ARTIFACT'
                echo '========================================'

                archiveArtifacts(
                    artifacts: 'target/*.jar',
                    fingerprint: true,
                    allowEmptyArchive: false
                )

                echo 'Artifact archived successfully.'
            }
        }


        // =========================================================
        // 7. DEPLOY
        // =========================================================
           
        stage('Deploy') {
    steps {

        bat '''
            echo ========================================
            echo DEPLOYING WOMEN SKILL TRAINING PORTAL
            echo ========================================

            echo.
            echo Jenkins:
            echo http://localhost:8080

            echo.
            echo Application:
            echo http://localhost:8081

            echo.
            echo Deployment directory:
            echo C:\\WSTP-Deployment

            echo.
            echo ========================================
            echo STOPPING PREVIOUS APPLICATION
            echo ========================================

            powershell -NoProfile -ExecutionPolicy Bypass -Command "$connections = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue; if ($connections) { foreach ($c in $connections) { Write-Host ('Stopping PID: ' + $c.OwningProcess); Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue } } else { Write-Host 'No previous application is running.' }"

            echo.
            echo Waiting for old application to stop...

            timeout /t 3 /nobreak >nul

            echo.
            echo ========================================
            echo PREPARING DEPLOYMENT DIRECTORY
            echo ========================================

            if not exist "C:\\WSTP-Deployment" (
                mkdir "C:\\WSTP-Deployment"
            )

            del /Q "C:\\WSTP-Deployment\\*.jar" 2>nul

            echo.
            echo ========================================
            echo COPYING NEW JAR
            echo ========================================

            copy /Y "target\\women-skill-training-portal-0.0.1-SNAPSHOT.jar" "C:\\WSTP-Deployment\\women-skill-training-portal-0.0.1-SNAPSHOT.jar"

            if %ERRORLEVEL% NEQ 0 (
                echo JAR COPY FAILED
                exit /b 1
            )

            echo.
            echo JAR copied successfully.

            echo.
            echo ========================================
            echo STARTING SPRING BOOT
            echo ========================================

            powershell -NoProfile -ExecutionPolicy Bypass -Command "$out='C:\\WSTP-Deployment\\application.log'; $err='C:\\WSTP-Deployment\\application-error.log'; Start-Process -FilePath 'java' -ArgumentList '-jar','C:\\WSTP-Deployment\\women-skill-training-portal-0.0.1-SNAPSHOT.jar','--server.port=8081' -WorkingDirectory 'C:\\WSTP-Deployment' -RedirectStandardOutput $out -RedirectStandardError $err -WindowStyle Hidden"

            echo.
            echo Spring Boot process started in background.

            echo.
            echo ========================================
            echo WAITING FOR APPLICATION
            echo ========================================

            powershell -NoProfile -ExecutionPolicy Bypass -Command "$success=$false; for($i=1;$i -le 30;$i++){ Write-Host ('Checking application... Attempt ' + $i + '/30'); try { $r=Invoke-WebRequest -Uri 'http://localhost:8081' -UseBasicParsing -TimeoutSec 3; Write-Host ('HTTP Status: ' + $r.StatusCode); if($r.StatusCode -ge 200 -and $r.StatusCode -lt 500){$success=$true; break} } catch { Write-Host 'Application not ready yet...' }; Start-Sleep -Seconds 2 }; if(-not $success){Write-Error 'Application failed to start'; exit 1}"

            if %ERRORLEVEL% NEQ 0 (
                echo.
                echo ========================================
                echo DEPLOYMENT FAILED
                echo ========================================

                echo.
                echo Application error log:
                type "C:\\WSTP-Deployment\\application-error.log" 2>nul

                exit /b 1
            )

            echo.
            echo ========================================
            echo APPLICATION IS RUNNING
            echo ========================================

            echo HTTP Status: 200

            echo.
            echo ========================================
            echo DEPLOYMENT VERIFIED SUCCESSFULLY
            echo ========================================

            echo.
            echo Application URL:
            echo http://localhost:8081

            echo.
            echo Deployment directory:
            echo C:\\WSTP-Deployment

            echo.
            echo Application logs:
            echo C:\\WSTP-Deployment\\application.log

            echo.
            echo ========================================
        '''
    }
}
                    

                   
        // =========================================================
        // 8. POST DEPLOYMENT
        // =========================================================

        stage('Post Actions') {
            steps {

                echo '========================================'
                echo 'POST DEPLOYMENT VERIFICATION'
                echo '========================================'

                bat '''
                    echo.
                    echo Jenkins:
                    echo http://localhost:8080

                    echo.
                    echo Women Skill Training Portal:
                    echo http://localhost:8081

                    echo.
                    echo Deployment completed successfully.
                '''
            }
        }
    }


    // =============================================================
    // PIPELINE POST ACTIONS
    // =============================================================

    post {

        success {

            echo '''
            ================================================
                    WSTP CI/CD PIPELINE SUCCESS
            ================================================

            Build       : SUCCESS
            Tests       : PASSED
            Reports     : PUBLISHED
            Package     : CREATED
            Artifact    : ARCHIVED
            Deployment  : SUCCESS

            Jenkins:
            http://localhost:8080

            Application:
            http://localhost:8081

            ================================================
            '''
        }


        failure {

            echo '''
            ================================================
                    WSTP CI/CD PIPELINE FAILED
            ================================================

            One or more pipeline stages failed.

            IMPORTANT:
            If the Test stage failed, deployment was blocked.

            Check:
            1. Console Output
            2. Test Results
            3. target/surefire-reports
            4. Jenkins Stage View

            ================================================
            '''
        }


        always {

            echo 'Publishing test report information...'

            archiveArtifacts(
                artifacts: 'target/surefire-reports/**/*',
                allowEmptyArchive: true,
                fingerprint: false
            )
        }
    }
}