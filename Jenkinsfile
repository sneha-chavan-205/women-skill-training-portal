pipeline {

    agent any

    environment {
        APP_NAME = "women-skill-training-portal"
        DEPLOY_DIR = "C:\\WSTP-Deployment"
        SERVER_PORT = "8081"
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out Women Skill Training Portal source code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the application...'
                bat 'mvn clean compile -DskipTests'
            }
        }

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

        stage('Package') {
            steps {
                echo 'Packaging Spring Boot application...'
                bat 'mvn package -DskipTests'
            }
        }

        stage('Archive Artifact') {
            steps {
                echo 'Archiving generated JAR...'

                archiveArtifacts artifacts: 'target/*.jar',
                                 fingerprint: true
            }
        }

        stage('Deploy') {
            steps {

                echo 'Deploying application...'

                bat '''
                if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

                copy /Y target\\*.jar "%DEPLOY_DIR%\\%APP_NAME%.jar"
                '''

                withCredentials([
                    string(
                        credentialsId: 'mysql-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {

                    powershell '''
                    $pidFile = "$env:DEPLOY_DIR\\app.pid"

                    # Stop previous application if running
                    if (Test-Path $pidFile) {

                        $oldPid = Get-Content $pidFile

                        try {
                            Stop-Process -Id $oldPid -Force -ErrorAction Stop
                            Write-Host "Stopped previous application process: $oldPid"
                        }
                        catch {
                            Write-Host "Previous process was not running."
                        }

                        Remove-Item $pidFile -Force
                    }

                    # JAR path
                    $jarPath = "$env:DEPLOY_DIR\\$env:APP_NAME.jar"

                    # Start Spring Boot application
                    $process = Start-Process `
                        -FilePath "java" `
                        -ArgumentList "-jar `"$jarPath`" --server.port=$env:SERVER_PORT" `
                        -WorkingDirectory $env:DEPLOY_DIR `
                        -PassThru

                    # Save PID
                    $process.Id | Out-File $pidFile

                    Write-Host "Application started with PID: $($process.Id)"
                    Write-Host "Application URL: http://localhost:$env:SERVER_PORT"
                    '''
                }
            }
        }
    }

    post {

        success {
            echo '======================================'
            echo 'WSTP CI/CD PIPELINE SUCCESSFUL'
            echo 'Application deployed successfully!'
            echo 'URL: http://localhost:8081'
            echo '======================================'
        }

        failure {
            echo 'Pipeline failed. Check the console output.'
        }
    }
}