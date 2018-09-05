# Pivotal Application Service > Service Inventory Report

[![Build Status](https://travis-ci.org/pacphi/cf-service-inventory-report.svg?branch=app-deploy)](https://travis-ci.org/pacphi/cf-service-inventory-report) [![Known Vulnerabilities](https://snyk.io/test/github/pacphi/cf-service-inventory-report/badge.svg)](https://snyk.io/test/github/pacphi/cf-service-inventory-report)

This is a Spring Boot application that employs the Reactive support in both the [Pivotal Application Service Java Client](https://github.com/cloudfoundry/cf-java-client) and your choice of either [Spring Boot Starter Data Mongodb](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive) or [rxjava2-jdbc](https://github.com/davidmoten/rxjava2-jdbc) with an [HSQL](http://www.hsqldb.org) backend.  These libraries are employed to generate custom service inventory detail and summary reports from a target foundation.  An email will be sent to recipient(s) with those reports attached. 

## Prerequisites

Required

* [Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) account 

Optional

* Email account
* SMTP Host
* [SendGrid](https://sendgrid.com/pricing/) account 

## Tools

* [git](https://git-scm.com/downloads) 2.17.1 or better
* [JDK](http://openjdk.java.net/install/) 8u162 or better
* [cf](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) CLI 6.37.0 or better

## Clone

```
git clone https://github.com/pacphi/cf-service-inventory-report.git
```

## How to configure

Edit the contents of the `application.yml` file located in `src/main/resources`.  You will need to provide administrator credentials to services Manager for the foundation if you want to get a complete inventory of services. 

> You really should not bundle configuration with the application. To take some of the sting away, you might consider externalizing and encrypting this configuration.

### Minimum required keys

At a minimum you should supply values for the following keys

* `cf.apiHost` - a Pivotal Application Service API endpoint
* `cf.username` - a Pivotal Application Service account username (typically an administrator account)
* `cf.password` - a Pivotal Application Service account password
* `notification.engine` - email provider, options are: `none`, `java-mail` or `sendgrid`

> If you set the email provider to `none`, then no email will be delivered

### for java-mail

* `spring.mail.host` - an SMTP host
* `spring.mail.username` - an email account username
* `spring.mail.password` - an email account password
* `mail.from` - originator email address 
* `mail.recipients` - email addresses that will be sent an email with CSV attachments

### for sendgrid

* `spring.sendgrid.api-key` - an api key for your SendGrid account
* `mail.from` - originator email address 
* `mail.recipients` - email addresses that will be sent an email with CSV attachments

### to choose between backends

Set `spring.profiles.active` to one of either `mongo` or `jdbc`.

E.g., you could start the service with an HSQL backend using

```
./gradlew bootRun -Dspring.profiles.active=jdbc
```

### to override the default download URL for Embedded Mongo

On application start-up, a versioned Mongo executable is downloaded from a default location (addressable from the public internet).  If you would like to download the executable from an alternate location and/or select an alternate version, add the following:

* `spring.mongodb.embedded.verson` - version of the Mongo executable (e.g., `3.4.15`)
* `spring.mongodb.embedded.download.path` - the path to the parent directory hosting OS-specific sub-directories and version(s) of Mongo executables (e.g., `https://fastdl.mongodb.org/`)
* `spring.mongodb.embedded.download.alternate` - this is a boolean property and must be set to true to activate alternate download URL

As an example, the following

```
spring:
  mongodb:
    embedded:
      version: 3.4.15
      download:
        path: https://fastdl.mongodb.org/
        alternate: true
```

would download the Mongo executable from `https://fastdl.mongodb.org/osx/mongodb-osx-x86_64-3.4.15.tgz` when the service is running on a Mac OSX host.

> OS-specific sub-directory choices are: `linux`, `win32`, and `osx`. See [https://www.mongodb.com/download-center#community](https://www.mongodb.com/download-center#community) for more details.

### to set the delivery Schedule

Update the value of the `cron` property in `application.yml`.  Consult this [article](https://www.baeldung.com/spring-scheduled-tasks) and the [Javadoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron--) to understand how to tune it for your purposes.

## How to Build

```
./gradlew build
```

## How to Run

```
./gradlew bootRun -Dspring.profiles.active={backend_provider}
```
where `{backend_provider}` is either `mongo` or `jdbc`

> You'll need to manually stop to the application with `Ctrl+C`

## How to deploy to Pivotal Application Service

You may choose to follow the step-by-step instructions below or execute a couple of shell scripts to [deploy](deploy.sh) and/or [shutdown/destroy](destroy.sh) the application.

Authenticate to a foundation using the API endpoint. 
> E.g., login to [Pivotal Web Services](https://run.pivotal.io)

```
cf login -a https:// api.run.pivotal.io
```

Push the app, but don't start it, also disable health check and routing.

```
cf push get-service-inventory-task -p ./build/libs/cf-service-inventory-report-0.1-SNAPSHOT.jar -m 1G --no-start
```

Set environment variable for backend

> You have a choice of backends, either `jdbc` or `mongo`

```
cf set-env get-service-inventory-task SPRING_PROFILES_ACTIVE jdbc
```

Start the app

```
cf start get-service-inventory-task
```

## What does the task do?

Utilizes cf CLI to query foundation for service details across all organizations and spaces for which the account is authorized.  Generates an email with a couple of attachments, then sends a copy to each recipient.

### Subject

Sample 

```
PCF Service Inventory Report
```

### Body

Sample 

```
Please find attached service inventory detail and summary reports from api.run.pivotal.io generated 2018-05-30T10:55:08.247.
```

### Attachments

#### Detail

Sample `service-inventory-detail.csv`

```
organization,space,name,service,plan,type,bound applications,last operation,last updated,dashboard url,requested state
"Northwest","sdeeg","cfex-mysql",,,,"user_provided",,,,,
"Northwest","nharris","scdf-mysql","cleardb","Highly available MySQL for your Apps.","spark","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=641f10e2-ef48-4634-ac41-e3d47a24fa54","2018-09-03T10:24:23","succeeded"
"Northwest","nharris","scdf-rabbit","cloudamqp","Managed HA RabbitMQ servers in the cloud","lemur","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=1d252d74-dc4a-45c3-8443-033dd86bd85d","2018-09-03T10:24:11","succeeded"
"Northwest","nharris","scdf-redis","rediscloud","Enterprise-Class Redis for Developers","30mb","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=3d3a5ece-8b0e-4155-bbbc-6d29d0a6e4ab","2018-09-03T10:24:04","succeeded"
"Northwest","wlund","fortunes-db","cleardb","Highly available MySQL for your Apps.","spark","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=a2bb1310-adb7-4d52-b2ed-8a2e216c57ca","2016-12-07T13:54:50","succeeded"
"Northwest","wlund","mysql","cleardb","Highly available MySQL for your Apps.","spark","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=20057992-46d9-4fcb-9e90-7ef0a79a7afc",,
"Northwest","wlund","analytics-de0d73fa-22fb-498b-b10e-55b432d1bbf9","p-dataflow-analytics","Proxies to the Spring Cloud Data Flow analytics service instance","proxy","managed",,"create",,"2017-11-28T13:45:37","succeeded"
"Northwest","wlund","messaging-de0d73fa-22fb-498b-b10e-55b432d1bbf9","p-dataflow-messaging","Proxies to the Spring Cloud Data Flow messaging service instance","proxy","managed",,"create",,"2017-11-28T13:45:37","succeeded"
"Northwest","wlund","cf-workshop-mongo","mlab","Fully managed MongoDB-as-a-Service","sandbox","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=2f34c733-c2ca-45de-b125-ce8a6c30074a","2017-10-03T13:39:09","succeeded"
"Northwest","wlund","dataflow-server","p-dataflow","Deploys Spring Cloud Data Flow servers to orchestrate data pipelines","standard","managed",,"create","https://p-dataflow.cfapps.io/instances/de0d73fa-22fb-498b-b10e-55b432d1bbf9/dashboard","2017-11-28T13:49:35","succeeded"
"Northwest","wlund","redis","rediscloud","Enterprise-Class Redis for Developers","30mb","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=55a30313-8596-4e97-8fce-7ad93777eb40","2017-10-11T19:08:20","succeeded"
"Northwest","wlund","relational-de0d73fa-22fb-498b-b10e-55b432d1bbf9","p-dataflow-relational","Proxies to the Spring Cloud Data Flow datastore service instance","proxy","managed",,"create",,"2017-11-28T13:45:37","succeeded"
"Northwest","wlund","postgres","elephantsql","PostgreSQL as a Service","turtle","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=6c463f7f-04ec-4fdb-90e8-056fd437b621",,
"Northwest","mkillens","autoscale-mkillens","app-autoscaler","Scales bound applications in response to load","standard","managed",,"create",,"2018-08-20T14:06:03","succeeded"
"Northwest","dkobel","attendee-service-DevLab-CUP",,,,"user_provided","articulate-DevLab",,,,
"Northwest","wlund","whocares","cleardb","Highly available MySQL for your Apps.","spark","managed",,"create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=8bc0070e-6c0a-4636-bebf-3a1c50c00609","2017-06-14T13:45:13","succeeded"
"Northwest","sdeeg","vevent-rds",,,,"user_provided","vevent-smd",,,,
"Northwest","mkillens","cuDirectTestConfiguration","p-config-server","Config Server for Spring Cloud Applications","trial","managed",,"create","https://spring-cloud-service-broker.cfapps.io/dashboard/p-config-server/571c792f-f102-4378-95e4-83ce26feb233","2018-08-28T15:48:15","succeeded"
"Northwest","sdeeg","drupal-db",,,,"user_provided","drupal8",,,,
"Northwest","bkamysz","my-db","elephantsql","PostgreSQL as a Service","turtle","managed","nodebroker","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=ff9f85a1-3e62-4e2b-b666-727e8ab6d690","2018-08-13T11:27:39","succeeded"
"Northwest","bkamysz","my-postgres","elephantsql","PostgreSQL as a Service","turtle","managed","nodebroker","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=b4228636-4f5f-4917-9b6a-0ddb7937eb6d","2018-08-13T13:53:22","succeeded"
"Northwest","bkamysz","my-redis","rediscloud","Enterprise-Class Redis for Developers","30mb","managed","nodebroker","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=8212deee-b357-4de2-961e-505e08d4b0e4","2018-08-14T16:20:29","succeeded"
"Northwest","bkamysz","my-mongo","mlab","Fully managed MongoDB-as-a-Service","sandbox","managed","nodebroker","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=b3cd69d9-e993-4f3f-b70b-019d302b8c76","2018-08-13T11:45:34","succeeded"
"Northwest","dkobel","attendee-mysql","cleardb","Highly available MySQL for your Apps.","spark","managed","attendee-service-DGK","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=6988578b-68cc-47d2-bc6e-d2f76bf08d54","2017-06-06T18:59:17","succeeded"
"Northwest","wlund","rabbit","cloudamqp","Managed HA RabbitMQ servers in the cloud","lemur","managed","pcfdemo","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=d5cc4dba-1ba5-4983-86db-57d469dda2ef","2018-07-19T13:32:15","succeeded"
"Northwest","dkobel","greeting-db","cleardb","Highly available MySQL for your Apps.","spark","managed","hello-spring-boot-rest","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=bf90d338-317f-4a4f-bf27-f8a35638b2ac",,
"Northwest","dkobel","CLtest","cloudamqp","Managed HA RabbitMQ servers in the cloud","lemur","managed","pcfdemo","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=6cb10772-ee68-4862-acff-592e49d0c769","2017-06-07T13:45:31","succeeded"
...
```

#### Summary

Sample `service-inventory-summary.csv`

```
organization,total
Northwest,40

service,total
cleardb,8
cloudamqp,5
p-config-server,5
p-service-registry,5
p-circuit-breaker-dashboard,3
mlab,2
rediscloud,2
service-autoscaler,2
elephantsql,1
p-dataflow-relational,1
p-dataflow-analytics,1
p-dataflow,1
p-dataflow-messaging,1
scheduler-for-pcf,1

last updated,services total
<= 1 day,0
> 1 day <= 1 week,0
> 1 week <= 1 month,8
> 1 month <= 3 months,3
> 3 months <= 6 months,7
> 6 months <= 1 year,14
> 1 year,0

Total services: 40
```

## Credits

Tip of the hat to those who've gone before...

* Baeldung [1](http://www.baeldung.com/spring-email), [2](http://www.baeldung.com/spring-events)
* [John Thompson](https://springframework.guru/spring-data-mongodb-with-reactive-mongodb/)
* [Josh Long](https://github.com/joshlong/cf-task-demo)
* [Mohit Sinha](https://github.com/mohitsinha/spring-boot-webflux-reactive-mongo)
* [Pas Apicella](http://theblasfrompas.blogspot.com/2017/03/run-spring-cloud-task-from-pivotal.html)
* [Robert Watkins](https://gist.github.com/twasink/3073710)
* [David Moten](https://github.com/davidmoten/rxjava2-jdbc)
* [Robert B Roeser](https://medium.com/netifi/spring-webflux-and-rxjava2-jdbc-83a94e71ba04)
