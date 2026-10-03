pipeline {

    agent any

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
                bat 'mvn test'
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
    }

    post {

        success {
            echo '=========================================='
            echo 'WSTP PIPELINE COMPLETED SUCCESSFULLY'
            echo '=========================================='
        }

        failure {
            echo '=========================================='
            echo 'WSTP PIPELINE FAILED'
            echo 'Check Jenkins Console Output'
            echo '=========================================='
        }

        always {
            echo 'Pipeline execution completed.'
        }
    }
}