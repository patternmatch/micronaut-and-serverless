## Prerequisities
1. Java 8 on your local machine.
2. Docker on your local machine.
3. AWS user for Serverless. In AWS Console, go to IAM (Identity and Access Management), select Users
and then click on "Add User". You can name your user "serverless-admin" with access type "Programmatic".
Attach existing policies directly -> "AdministratorAccess". And finalize. You will receive
a file "credentials.csv" with user Access Key ID and Secret Access Key. Grab them.
4. Update (or create if missing) your ~/.aws/credentials file. Add following section:
```bash
[serverless-admin]
aws_access_key_id = {keyId}
aws_secret_access_key = {secretKey}
```
and replace placeholders with keys of your newly created user.

## Initial run and one-time-setup.
1. Go to project directory.
2. Run `./gradlew clean` - this will intialize Gradle and download half the Internet, so be patient.
3. Run `./gradlew buildDockerImageForSls` - this will use existing Dockerfile to build Docker image that will be used 
to run Serverless commands. 

## Building and deployment
There are 2 subprojects:
1. micronaut-lambda - this one creates regular Lambda function with API Gateway 
configured as "Lambda" - POSTing to API gateway will invoke Java/Kotlin function annotated
with @FunctionBean and incoming Json payload will be automatically converted to JVM class.
2. micronaut-lambda-proxy - this one creates Lambda proxy which passes requests to 
Micronaut REST controllers. API Gateway is configured as "Lambda-Proxy" and it 
transparently passes incoming request to the Lambda (path, headers, query params, body). 
Micronaut will invoke matching method and provide all mapping to required API Gateway formats.

To build proper lambda jar file run `./gradlew :sub-project:build` for example: 
`./gradlew :micronaut-lambda:build`. 
To deploy to AWS, with full stack deployment: `./gradlew :micronaut-lambda:slsDeploy`.
To clean up all AWS resources: `./gradlew :micronaut-lambda:slsRemove`.

`:sub-project:slsDeploy` task depends on `:sub-project:build` so there is not need for explicit build before deploy, it will 
be done automatically by Gradle.

For convenience I created shell scripts, called `mn_lam_[prx]_[deploy|remove].sh` so 
you don't need to craft Gradle subprojects and tasks by hand.

If the only change is in Lambda function code, deployment can be optimized. Run: `./gradlew -PfunName=<function-name> :sub-project:slsDeployFunction`
This will not recreate whole stack but only push new version of jar with new version of Lambda function.
For convenience there are 2 scripts `mn_lam_[prx]_deploy_function.sh` - the scripts take one parameter with function name.

## Micronaut-lambda testing
`./gradlew :micronaut-lambda:slsDeploy` tasks ends with outputting API Gateway endpoint to the console.
It is different for each full stack deployment. An example from my run is:
```bash
endpoints:
  POST - https://gtonm0p1zb.execute-api.us-east-1.amazonaws.com/dev/testendpoint
```
To test everything, curl call can be made:
```bash
$ curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"someValue":5,"message":"my message"}' \
  https://gtonm0p1zb.execute-api.us-east-1.amazonaws.com/dev/testendpoint
```
The input Json contains someValue and message fields.
The response should be:
```bash
{"outValue":5,"outMessage":"my message","someBool":true}
```
It contains some constant values and our input message.

## Micronaut-lambda-proxy testing
`./gradlew :micronaut-lambda-proxy:slsDeploy` tasks ends with outputting API Gateway endpoint to the console.
It is different for each full stack deployment. An example from my run is:
```bash
endpoints:
  ANY - https://n1p7z0n563.execute-api.us-east-1.amazonaws.com/dev/{proxy+}
```
To test everything, curl call can be made:
```bash
$ curl https://n1p7z0n563.execute-api.us-east-1.amazonaws.com/dev/ping
```
The response should be:
```bash
{"pong":true}
```
