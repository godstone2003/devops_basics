# Jenkins CI/CD Pipeline

This repository contains Jenkins pipeline files used to automate the Continuous Integration and Continuous Deployment (CI/CD) process for our Java-based application. The pipeline automates tasks such as building the project, running tests, analyzing code quality, creating Docker images, and deploying the application to AWS ECS. Below are the details for each Jenkins pipeline file in this repository.

## Files in this Repository

### 1. **Jenkinsfile**

The main Jenkins pipeline file (`Jenkinsfile`) is used to define the CI/CD workflow for the application. It includes multiple stages to automate the build and deployment processes. The file contains the following sections:

- **Tools Configuration**: Specifies the required Maven and JDK versions for building the project.
- **Slack Notification Integration**: Sends a notification to Slack with build results (success or failure).
- **Stages**:
  - **Test Slack**: A placeholder step to test Slack integration.
  - **Fetch Code**: Clones the code from the GitHub repository.
  - **Build**: Runs the Maven build command to compile the project and create a WAR file.
  - **Unit Test**: Runs unit tests using Maven.
  - **Checkstyle Analysis**: Checks the code for style violations using the Checkstyle plugin.
  - **SonarQube Code Analysis**: Runs SonarQube analysis to evaluate code quality.
  - **Quality Gate**: Ensures the build passes the quality gate from SonarQube.
  - **Upload Artifact**: Uploads the built artifact (WAR file) to Nexus repository.
  - **Deploy to ECS**: Deploys the application to AWS ECS using the `aws ecs` CLI.

### 2. **Dockerfile**

The `Dockerfile` defines the steps to build a Docker image for the application. It includes instructions to:
- Use a base image with Java runtime.
- Copy the WAR file into the Docker container.
- Expose the necessary port for the application to run.
- Set the entry point to start the application when the container is run.

This Dockerfile is used in the **Build App Image** stage of the Jenkins pipeline to build a Docker image of the application and push it to Amazon ECR.

### 3. **AWS ECS Configuration**

This file contains the configuration required to deploy the application to AWS ECS (Elastic Container Service). It includes:
- The ECS cluster and service names.
- AWS CLI commands used to trigger a new deployment of the application.
- Necessary environment variables for configuring the AWS CLI during the deployment stage.

### Prerequisites

To use these Jenkins pipelines, ensure the following tools and services are set up:

1. **Jenkins**: Set up Jenkins on your server with the necessary plugins installed (e.g., Docker, Maven, SonarQube, Slack).
2. **AWS Account**: Ensure you have an AWS account set up with permissions for ECS, ECR, and other necessary services.
3. **SonarQube**: Set up a SonarQube server for static code analysis.
4. **Slack**: Integrate Jenkins with Slack to receive notifications.
5. **Nexus Repository**: Set up a Nexus repository for artifact management.

### How to Use

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/jenkins-pipeline-repo.git
   
2 .Configure Jenkins:

Create a new Jenkins pipeline job and point it to this repository.
Set up the necessary environment variables and credentials in Jenkins (e.g., awscreds, nexus-credentials).
Ensure that Maven, JDK, Docker, SonarQube, and AWS CLI are installed and configured on your Jenkins instance.
Run the pipeline: Once the Jenkins job is configured, trigger the pipeline to start the CI/CD process. It will automatically run through all the stages from code checkout to deployment.

License
This project is licensed under the MIT License - see the LICENSE file for details.

Contact
For any issues or questions, feel free to open an issue or contact the repository maintainers.


### Key Points Covered in the README:

- **Overview of the Repository**: Provides an explanation of what the repository contains and what each file does.
- **File Descriptions**: Detailed descriptions of the **Jenkinsfile**, **Dockerfile**, and **AWS ECS Configuration**.
- **Prerequisites**: Lists the required tools and services (Jenkins, AWS, SonarQube, Slack, Nexus) needed to run the pipeline.
- **How to Use**: Step-by-step instructions on how to clone the repository, configure Jenkins, and trigger the pipeline.
- **License and Contact**: Mentions the license and how to contact the maintainers or report issues.

You can replace the placeholders such as repository URLs or contact details as necessary for your use case.

