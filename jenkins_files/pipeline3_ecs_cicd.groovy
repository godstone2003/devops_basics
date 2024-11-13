pipeline {
    agent any
    tools {
        maven "MAVEN3.9" // Specify the Maven version to use
        jdk "JDK17" // Specify the JDK version to use
    }

    environment {
        registryCredential = 'ecr:us-east-1:awscreds' // AWS ECR credentials
        appRegistry = "636167220663.dkr.ecr.us-east-1.amazonaws.com/randomappimg" // ECR app registry URL
        vprofileRegistry = "https://636167220663.dkr.ecr.us-east-1.amazonaws.com" // ECR registry URL
        cluster = "random-cluster" // ECS cluster name
        service = "random-app-svc" // ECS service name
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
                git branch: 'docker', url: 'https://github.com/randomuser/random-project.git' // Replace with your Git details
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
                scannerHome = tool 'sonar6.2' // Specify your SonarQube scanner version
                SONAR_SCANNER_OPTS = '-Djava.security.egd=file:/dev/./urandom --add-opens=java.base/java.lang=ALL-UNNAMED'
            }
            steps {
                // Run SonarQube analysis
                withSonarQubeEnv('sonarserver') { // Replace with your SonarQube server name
                    sh '''${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=randomproject \
                       -Dsonar.projectName=randomproject \
                       -Dsonar.projectVersion=1.0 \
                       -Dsonar.sources=src/ \
                       -Dsonar.java.binaries=target/test-classes/com/randomuser/account/controllerTest/ \
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

        stage('Deploy to ECS') {
            steps {
                // Deploy the app image to ECS
                withAWS(credentials: 'awscreds', region: 'us-east-1') {
                    sh 'aws ecs update-service --cluster ${cluster} --service ${service} --force-new-deployment'
                }
            }
        }
    }
}




/**

1.Slack Test Stage: This stage remains a placeholder for testing the Slack integration.
2.Code Fetching: The Fetch Code stage fetches code from your GitHub repository.
3.Build, Test, and Code Analysis: Standard stages for building the project, running tests, performing code analysis using Checkstyle and SonarQube.
4.Build and Push Docker Image: The stages for building the Docker image using the Dockerfile and pushing the image to AWS ECR.
5.Deploy to ECS: The new stage uses AWS CLI to deploy the new Docker image to an ECS service, with the command aws ecs update-service.
Ensure that your AWS credentials and SonarQube configuration are properly set up for this to work seamlessly.

**/