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

                echo '=========================================='
                echo 'CHECKOUT'
                echo '=========================================='

                echo 'Checking out Women Skill Training Portal source code...'

                checkout scm
            }
        }


        // =====================================================
        // 2. BUILD
        // =====================================================
        stage('Build') {
            steps {

                echo '=========================================='
                echo 'BUILD'
                echo '=========================================='

                echo 'Building the application...'

                bat 'mvn clean compile -DskipTests'
            }
        }


        // =====================================================
        // 3. TEST
        // =====================================================
        stage('Test') {
            steps {

                echo '=========================================='
                echo 'TEST'
                echo '=========================================='

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

                echo '=========================================='
                echo 'PACKAGE'
                echo '=========================================='

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

                echo '=========================================='
                echo 'ARCHIVE ARTIFACT'
                echo '=========================================='

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

                echo '=========================================='
                echo 'DEPLOY'
                echo '=========================================='

                echo 'Deploying Spring Boot application...'

                // ---------------------------------------------
                // Create deployment directory
                // ---------------------------------------------
                bat '''
                if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
                '''

                // ---------------------------------------------
                // Copy JAR
                // ---------------------------------------------
                bat '''
                echo Copying application JAR...

                copy /Y target\\women-skill-training-portal-0.0.1-SNAPSHOT.jar "%DEPLOY_DIR%\\%APP_NAME%.jar"
                '''

                // ---------------------------------------------
                // Database credentials
                // ---------------------------------------------
                withCredentials([
                    string(
                        credentialsId: 'mysql-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {

                    powershell '''
                    $pidFile = "$env:DEPLOY_DIR\\app.pid"
                    $outLog = "$env:DEPLOY_DIR\\app.out.log"
                    $errLog = "$env:DEPLOY_DIR\\app.err.log"

                    $jarPath = "$env:DEPLOY_DIR\\$env:APP_NAME.jar"

                    Write-Host "=========================================="
                    Write-Host "STOPPING PREVIOUS APPLICATION"
                    Write-Host "=========================================="

                    # Stop previous application if PID file exists
                    if (Test-Path $pidFile) {

                        $oldPid = Get-Content $pidFile

                        Write-Host "Previous PID: $oldPid"

                        try {

                            $oldProcess = Get-Process -Id $oldPid -ErrorAction Stop

                            Stop-Process -Id $oldPid -Force

                            Write-Host "Previous application stopped successfully."

                        }
                        catch {

                            Write-Host "Previous application process was not running."
                        }

                        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
                    }


                    Write-Host "=========================================="
                    Write-Host "CLEANING OLD LOG FILES"
                    Write-Host "=========================================="

                    if (Test-Path $outLog) {
                        Remove-Item $outLog -Force
                    }

                    if (Test-Path $errLog) {
                        Remove-Item $errLog -Force
                    }


                    Write-Host "=========================================="
                    Write-Host "STARTING SPRING BOOT APPLICATION"
                    Write-Host "=========================================="

                    Write-Host "JAR: $jarPath"
                    Write-Host "PORT: $env:SERVER_PORT"


                    # Start Spring Boot application
                    $process = Start-Process `
                        -FilePath "java" `
                        -ArgumentList "-jar `"$jarPath`" --server.port=$env:SERVER_PORT" `
                        -WorkingDirectory $env:DEPLOY_DIR `
                        -RedirectStandardOutput $outLog `
                        -RedirectStandardError $errLog `
                        -PassThru


                    # Save PID
                    $process.Id | Out-File $pidFile

                    Write-Host "Spring Boot process started."
                    Write-Host "PID: $($process.Id)"


                    Write-Host "=========================================="
                    Write-Host "WAITING FOR APPLICATION TO START"
                    Write-Host "=========================================="


                    $maxAttempts = 30
                    $started = $false


                    for ($i = 1; $i -le $maxAttempts; $i++) {

                        Start-Sleep -Seconds 2

                        Write-Host "Checking application... Attempt $i/$maxAttempts"


                        # Check whether Java process is still running
                        $runningProcess = Get-Process `
                            -Id $process.Id `
                            -ErrorAction SilentlyContinue


                        if (-not $runningProcess) {

                            Write-Host ""
                            Write-Host "=========================================="
                            Write-Host "SPRING BOOT PROCESS STOPPED"
                            Write-Host "=========================================="

                            if (Test-Path $outLog) {

                                Write-Host ""
                                Write-Host "APPLICATION OUTPUT:"
                                Get-Content $outLog -Tail 100
                            }

                            if (Test-Path $errLog) {

                                Write-Host ""
                                Write-Host "APPLICATION ERRORS:"
                                Get-Content $errLog -Tail 100
                            }

                            exit 1
                        }


                        # Check HTTP connection
                        try {

                            $response = Invoke-WebRequest `
                                -Uri "http://localhost:$env:SERVER_PORT" `
                                -UseBasicParsing `
                                -TimeoutSec 3 `
                                -ErrorAction Stop


                            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {

                                $started = $true

                                Write-Host ""
                                Write-Host "=========================================="
                                Write-Host "APPLICATION IS RUNNING"
                                Write-Host "=========================================="

                                Write-Host "HTTP Status: $($response.StatusCode)"

                                break
                            }

                        }
                        catch {

                            # Even a 4xx response means the web server is alive.
                            if ($_.Exception.Response -ne $null) {

                                $statusCode = [int]$_.Exception.Response.StatusCode

                                if ($statusCode -ge 400 -and $statusCode -lt 500) {

                                    $started = $true

                                    Write-Host ""
                                    Write-Host "Spring Boot is responding."
                                    Write-Host "HTTP Status: $statusCode"

                                    break
                                }
                            }

                            Write-Host "Application not ready yet..."
                        }
                    }


                    # -----------------------------------------
                    # Deployment verification
                    # -----------------------------------------
                    if (-not $started) {

                        Write-Host ""
                        Write-Host "=========================================="
                        Write-Host "DEPLOYMENT VERIFICATION FAILED"
                        Write-Host "=========================================="

                        Write-Host "Application did not respond on port $env:SERVER_PORT."

                        Write-Host ""
                        Write-Host "APPLICATION OUTPUT:"

                        if (Test-Path $outLog) {
                            Get-Content $outLog -Tail 100
                        }

                        Write-Host ""
                        Write-Host "APPLICATION ERRORS:"

                        if (Test-Path $errLog) {
                            Get-Content $errLog -Tail 100
                        }

                        Stop-Process `
                            -Id $process.Id `
                            -Force `
                            -ErrorAction SilentlyContinue

                        exit 1
                    }


                    Write-Host ""
                    Write-Host "=========================================="
                    Write-Host "DEPLOYMENT VERIFIED SUCCESSFULLY"
                    Write-Host "=========================================="

                    Write-Host "Application URL:"
                    Write-Host "http://localhost:$env:SERVER_PORT"

                    Write-Host "Application PID:"
                    Write-Host $process.Id

                    Write-Host "Deployment directory:"
                    Write-Host $env:DEPLOY_DIR

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

Application deployed and verified successfully.

URL:
http://localhost:8081

Jenkins has successfully completed:

1. Checkout
2. Build
3. Test
4. Package
5. Archive Artifact
6. Deploy
7. Deployment Health Check

==========================================
'''
        }


        failure {

            echo '''
==========================================
WSTP CI/CD PIPELINE FAILED
==========================================

Check the Jenkins Console Output.

The deployment logs are also available in:

C:\\WSTP-Deployment

==========================================
'''
        }


        always {

            echo '=========================================='
            echo 'Pipeline execution completed.'
            echo '=========================================='
        }
    }
}