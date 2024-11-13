// Define a color map for Slack notifications
def COLOR_MAP = [
    'SUCCESS': 'good',  // Green color for successful builds
    'FAILURE': 'danger' // Red color for failed builds
]

pipeline {
    agent any
    tools {
        maven "MAVEN3.9" // Define your Maven version as installed in Jenkins
        jdk "JDK17" // Define your JDK version as installed in Jenkins
    }

    stages {
        stage('Test Slack') {
            steps {
                // Placeholder command to test the Slack integration or run any setup task
                sh 'echo "Testing Slack integration or running a placeholder command."'
            }
        }

        stage('Fetch Code') {
            steps {
                // Fetch the code from the specified Git branch and repository
                git branch: 'main', url: 'https://github.com/random-user/random-repo.git' // Default branch and GitHub URL
            }
        }

        stage('Build') {
            steps {
                // Build the project using Maven and skip the tests
                sh 'mvn install -DskipTests'
            }
            post {
                success {
                    // Archive the built WAR file on a successful build
                    echo 'Now Archiving it...'
                    archiveArtifacts artifacts: '**/target/*.war'
                }
            }
        }

        stage('Unit Test') {
            steps {
                // Run unit tests using Maven
                sh 'mvn test'
            }
        }

        stage('Checkstyle Analysis') {
            steps {
                // Perform code style checks using Maven Checkstyle plugin
                sh 'mvn checkstyle:checkstyle'
            }
        }

        stage("Sonar Code Analysis") {
            environment {
                // Define the path to the SonarQube scanner and set additional options
                scannerHome = tool 'sonar-scanner-4.6' // Default SonarQube scanner version
                SONAR_SCANNER_OPTS = '-Djava.security.egd=file:/dev/./urandom --add-opens=java.base/java.lang=ALL-UNNAMED'
            }
            steps {
                // Run SonarQube analysis
                withSonarQubeEnv('sonar-default') { // Default SonarQube server name
                    sh '''${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=random-project \
                       -Dsonar.projectName=random-project \
                       -Dsonar.projectVersion=1.0 \
                       -Dsonar.sources=src/ \
                       -Dsonar.java.binaries=target/test-classes/com/example/controllerTest/ \
                       -Dsonar.junit.reportsPath=target/surefire-reports/ \
                       -Dsonar.jacoco.reportsPath=target/jacoco.exec \
                       -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml'''
                }
            }
        }

        stage("Quality Gate") {
            steps {
                // Wait for the quality gate results from SonarQube, with a timeout
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage("Upload Artifact") {
            steps {
                // Upload the built artifact to Nexus
                nexusArtifactUploader(
                    nexusVersion: 'nexus3', // Nexus version
                    protocol: 'http', // Protocol for Nexus connection
                    nexusUrl: '127.0.0.1:8081', // Default Nexus URL
                    groupId: 'com.example', // Default group ID
                    version: "${env.BUILD_ID}-${env.BUILD_TIMESTAMP}", // Versioning using build ID and timestamp
                    repository: 'example-repo', // Default Nexus repository
                    credentialsId: 'nexus-credentials', // Default credentials ID for Nexus
                    artifacts: [
                        [
                            artifactId: 'example-app', // Default artifact ID
                            classifier: '', // Classifier (optional, can be left empty)
                            file: 'target/example-app.war', // Default path to the WAR file
                            type: 'war' // Type of the artifact
                        ]
                    ]
                )
            }
        }
    }

    post {
        always {
            // Send a Slack notification after the pipeline finishes, regardless of the result
            echo 'Sending Slack Notifications...'
            slackSend (
                channel: '#random-channel', // Default Slack channel name
                color: COLOR_MAP[currentBuild.currentResult], // Color based on build result
                message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} \nMore info at: ${env.BUILD_URL}" // Notification message
            )
        }
    }
}


/**
This pipeline automates the process of building a Java application, running tests, analyzing code quality, and uploading the artifact to a Nexus repository.
 It also integrates Slack for notifications, uses SonarQube for code analysis, and checks the quality gate before proceeding.
 **/