pipeline {
    agent any
    tools {
        maven "MAVEN3.9" // Specify the Maven version to use
        jdk "JDK17" // Specify the JDK version to use
    }

    environment {
        registryCredential = 'ecr:us-east-1:awscreds' // AWS ECR credentials
        appRegistry = "123456789012.dkr.ecr.us-east-1.amazonaws.com/sampleappimg" // ECR app registry URL
        vprofileRegistry = "https://123456789012.dkr.ecr.us-east-1.amazonaws.com" // ECR registry URL
    }

    stages {
        stage('Test Slack') {
            steps {
                // Test Slack integration or run a placeholder command
                sh 'echo "Testing Slack integration or running a placeholder command."'
            }
        }

        stage('Fetch Code') {
            steps {
                // Fetch the code from the specified Git branch and repository
                git branch: 'main', url: 'https://github.com/random-user/sample-project.git' // Replace with your Git details
            }
        }

        stage('Build') {
            steps {
                // Build the project using Maven, skipping tests
                sh 'mvn install -DskipTests'
            }
            post {
                success {
                    // Archive the built WAR file if the build is successful
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
                // Perform Checkstyle analysis using Maven
                sh 'mvn checkstyle:checkstyle'
            }
        }

        stage("Sonar Code Analysis") {
            environment {
                // Set the path for the SonarQube scanner and options
                scannerHome = tool 'sonar-scanner-4.6' // Specify your SonarQube scanner version
                SONAR_SCANNER_OPTS = '-Djava.security.egd=file:/dev/./urandom --add-opens=java.base/java.lang=ALL-UNNAMED'
            }
            steps {
                // Run SonarQube analysis
                withSonarQubeEnv('sonar-default') { // Replace with your SonarQube server name
                    sh '''${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=sample-project \
                       -Dsonar.projectName=sample-project \
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
                // Wait for the quality gate status from SonarQube with a timeout
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build App Image') {
            steps {
                script {
                    // Build the Docker image from the specified directory
                    dockerImage = docker.build(appRegistry + ":$BUILD_NUMBER", "./Docker-files/app/multistage/")
                }
            }
        }

        stage('Upload App Image') {
            steps {
                script {
                    // Push the Docker image to AWS ECR
                    docker.withRegistry(vprofileRegistry, registryCredential) {
                        dockerImage.push("$BUILD_NUMBER")
                        dockerImage.push('latest')
                    }
                }
            }
        }

        stage('Remove Container Image') {
            steps {
                // Remove all Docker images to free up space
                sh 'docker rmi -f $(docker images -a -q)'
            }
        }
    }
}
