pipeline {

    agent any

    environment {
        APP_NAME = "women-skill-training-portal"
        DEPLOY_DIR = "C:\\WSTP-Deployment"
        SERVER_PORT = "8081"
    }

    stages {

        // =====================================================
        // 1. CHECKOUT
        // =====================================================
        stage('Checkout') {
            steps {
                echo 'Checking out Women Skill Training Portal source code...'

                checkout scm
            }
        }


        // =====================================================
        // 2. BUILD
        // =====================================================
        stage('Build') {
            steps {
                echo 'Building the application...'

                bat 'mvn clean compile -DskipTests'
            }
        }


        // =====================================================
        // 3. TEST
        // =====================================================
        stage('Test') {
            steps {

                echo 'Running application tests...'

                withCredentials([
                    string(
                        credentialsId: 'mysql-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {

                    bat 'mvn test'
                }
            }
        }


        // =====================================================
        // 4. PACKAGE
        // =====================================================
        stage('Package') {
            steps {

                echo 'Packaging Spring Boot application...'

                bat 'mvn clean package -DskipTests'

                echo 'Checking generated JAR...'

                bat '''
                echo ==========================================
                echo GENERATED JAR FILE
                echo ==========================================

                dir target\\*.jar

                echo.
                echo Checking executable Spring Boot JAR...
                jar tf target\\women-skill-training-portal-0.0.1-SNAPSHOT.jar | findstr "BOOT-INF"

                echo.
                echo JAR verification completed.
                '''
            }
        }


        // =====================================================
        // 5. ARCHIVE ARTIFACT
        // =====================================================
        stage('Archive Artifact') {
            steps {

                echo 'Archiving generated JAR...'

                archiveArtifacts(
                    artifacts: 'target/women-skill-training-portal-0.0.1-SNAPSHOT.jar',
                    fingerprint: true
                )
            }
        }


        // =====================================================
        // 6. DEPLOY
        // =====================================================
        stage('Deploy') {
            steps {

                echo 'Deploying application...'


                // -------------------------------------------------
                // Create deployment directory and copy JAR
                // -------------------------------------------------
                bat '''
                echo ==========================================
                echo PREPARING DEPLOYMENT DIRECTORY
                echo ==========================================

                if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

                echo.
                echo Copying Spring Boot executable JAR...

                copy /Y "target\\women-skill-training-portal-0.0.1-SNAPSHOT.jar" "%DEPLOY_DIR%\\%APP_NAME%.jar"

                echo.
                echo ==========================================
                echo DEPLOYED JAR DETAILS
                echo ==========================================

                dir "%DEPLOY_DIR%\\%APP_NAME%.jar"
                '''


                // -------------------------------------------------
                // Database credential
                // -------------------------------------------------
                withCredentials([
                    string(
                        credentialsId: 'mysql-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {

                    powershell '''
                    $ErrorActionPreference = "Stop"

                    $deployDir = $env:DEPLOY_DIR
                    $jarPath = "$deployDir\\$env:APP_NAME.jar"
                    $pidFile = "$deployDir\\app.pid"

                    Write-Host "=========================================="
                    Write-Host "WSTP APPLICATION DEPLOYMENT"
                    Write-Host "=========================================="

                    Write-Host "Deployment directory: $deployDir"
                    Write-Host "JAR path: $jarPath"
                    Write-Host "Server port: $env:SERVER_PORT"

                    # -------------------------------------------------
                    # Verify JAR exists
                    # -------------------------------------------------

                    if (!(Test-Path $jarPath)) {
                        throw "Deployment JAR was not found: $jarPath"
                    }

                    # -------------------------------------------------
                    # Verify JAR is not suspiciously small
                    # -------------------------------------------------

                    $jar = Get-Item $jarPath

                    Write-Host "JAR size: $($jar.Length) bytes"

                    if ($jar.Length -lt 1000000) {
                        throw "Deployment JAR appears invalid or incomplete. Size: $($jar.Length) bytes"
                    }

                    Write-Host "JAR size verification passed."

                    # -------------------------------------------------
                    # Stop previous application
                    # -------------------------------------------------

                    if (Test-Path $pidFile) {

                        $oldPid = Get-Content $pidFile

                        Write-Host "Previous PID found: $oldPid"

                        try {

                            Stop-Process `
                                -Id $oldPid `
                                -Force `
                                -ErrorAction Stop

                            Write-Host "Stopped previous application process: $oldPid"

                        }
                        catch {

                            Write-Host "Previous process was not running."
                        }

                        Remove-Item $pidFile -Force
                    }

                    # -------------------------------------------------
                    # Start Spring Boot application
                    # -------------------------------------------------

                    Write-Host "Starting Spring Boot application..."

                    $arguments = "-jar `"$jarPath`" --server.port=$env:SERVER_PORT"

                    $process = Start-Process `
                        -FilePath "java" `
                        -ArgumentList $arguments `
                        -WorkingDirectory $deployDir `
                        -PassThru

                    # -------------------------------------------------
                    # Save process ID
                    # -------------------------------------------------

                    $process.Id | Out-File $pidFile

                    Write-Host "Application started."
                    Write-Host "PID: $($process.Id)"
                    Write-Host "URL: http://localhost:$env:SERVER_PORT"

                    # -------------------------------------------------
                    # Give Spring Boot time to start
                    # -------------------------------------------------

                    Write-Host "Waiting for application startup..."

                    Start-Sleep -Seconds 10

                    # -------------------------------------------------
                    # Verify process
                    # -------------------------------------------------

                    $runningProcess = Get-Process `
                        -Id $process.Id `
                        -ErrorAction SilentlyContinue

                    if ($null -eq $runningProcess) {

                        throw "Spring Boot process stopped unexpectedly."

                    }

                    Write-Host "Spring Boot process is running."

                    # -------------------------------------------------
                    # Verify port
                    # -------------------------------------------------

                    $connection = Get-NetTCPConnection `
                        -LocalPort $env:SERVER_PORT `
                        -State Listen `
                        -ErrorAction SilentlyContinue

                    if ($null -eq $connection) {

                        throw "Application is not listening on port $env:SERVER_PORT"

                    }

                    Write-Host "Port $env:SERVER_PORT is listening."

                    # -------------------------------------------------
                    # Final deployment information
                    # -------------------------------------------------

                    Write-Host "=========================================="
                    Write-Host "DEPLOYMENT SUCCESSFUL"
                    Write-Host "=========================================="
                    Write-Host "Application URL: http://localhost:$env:SERVER_PORT"
                    Write-Host "Process ID: $($process.Id)"
                    Write-Host "JAR: $jarPath"
                    Write-Host "=========================================="
                    '''
                }
            }
        }
    }


    // =====================================================
    // POST ACTIONS
    // =====================================================

    post {

        success {

            echo '''
==========================================
WSTP CI/CD PIPELINE SUCCESSFUL
==========================================

Application deployed successfully.

URL:
http://localhost:8081

Jenkins has successfully completed:

1. Checkout
2. Build
3. Test
4. Package
5. Archive Artifact
6. Deploy

==========================================
'''
        }

        failure {

            echo '''
==========================================
WSTP CI/CD PIPELINE FAILED
==========================================

Check the Jenkins Console Output.

==========================================
'''
        }

        always {

            echo 'Pipeline execution completed.'
        }
    }
}