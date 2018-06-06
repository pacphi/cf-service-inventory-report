# Pivotal Application Service > Service Inventory Report

This is a [Spring Cloud Task](http://cloud.spring.io/spring-cloud-task/) that employs the Reactive support in both the [Pivotal Application Service Java Client](https://github.com/cloudfoundry/cf-java-client) and [Spring Boot Starter Data Mongodb](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive) libraries to generate custom service inventory detail and summary reports from a target foundation.  An email will be sent to recipient(s) with those reports attached. 

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
git clone https://github.com/pacphi/cf-get-service-details.git
```

## How to configure

Edit the contents of the `application.yml` file located in `src/main/resources`.  You will need to provide administrator credentials to Apps Manager for the foundation if you want to get a complete inventory of services. 

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


## How to Build

```
./gradlew build
```

## How to Run

```
./gradlew bootRun
```
> You'll need to manually stop to the application with `Ctrl+C`

## How to deploy to Pivotal Application Service

Authenticate to a foundation using the API endpoint. 
> E.g., login to [Pivotal Web Services](https://run.pivotal.io)

```
cf login -a https:// api.run.pivotal.io
```

Push the app disabling health check and routing.

```
cf push get-service-details-task --no-route --health-check-type none -p ./build/libs/cf-get-service-details-0.0.1-SNAPSHOT.jar -m 1G
```


## How to run as a task on Pivotal Application Service

To run the task

```
cf run-task get-service-details-task ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
```

To validate that the task ran successfully

```
cf logs get-service-details-task --recent
```


## How to schedule the task on Pivotal Application Service

Let's employ the [job scheduler](https://docs.pivotal.io/pcf-scheduler/1-1/using.html).

Create the service instance

```
cf create-service scheduler-for-pcf standard get-service-details-job
```

Bind the service instance to the task

```
cf bind-service get-service-details-task get-service-details-job
```

You'll need the Pivotal Application Service [job scheduler plugin for the cf CLI](https://network.pivotal.io/products/p-scheduler-for-pcf). Once the cf CLI plugin is installed, you can create jobs.

```
cf create-job get-service-details-task get-service-details-scheduled-job ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
```

To execute the job

```
cf run-job get-service-details-scheduled-job
```

To adjust the schedule for the job using a CRON-like expression (`MIN` `HOUR` `DAY-OF-MONTH` `MONTH` `DAY-OF-WEEK`)

```
cf schedule-job get-service-details-scheduled-job "0 8 ? * * "
```

Consult the [User Guide](https://docs.pivotal.io/pcf-scheduler/1-1/using-jobs.html) for other commands.

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
Please find attached application inventory detail and summary reports from api.run.pivotal.io generated 2018-05-30T10:55:08.247.
```

### Attachments

#### Detail

Sample `service-inventory-detail.csv`

```
organization,space,name,service,plan,type,last operation,last updated,dashboard url,requested state
"Northwest","sdeeg","help",,,"user_provided",,,,
"zoo-labs","test","get-app-details-job","scheduler-for-pcf","standard","managed","create",,"2018-05-15T23:17:36","succeeded"
"Northwest","wlund","cassandra",,,"user_provided",,,,
"Northwest","mkillens","autoscale-mkillens","app-autoscaler","standard","managed","create",,"2018-04-25T19:26:35","succeeded"
"Northwest","mkillens","myConfigServer","p-config-server","standard-legacy","managed","update","https://spring-cloud-service-broker.cfapps.io/dashboard/p-config-server/ae25bf5b-973c-4a61-b5f6-fb7b6566516a","2018-04-25T14:14:43","succeeded"
"Northwest","mkillens","myMySqlService","cleardb","spark","managed","create","https://cloudfoundry.appdirect.com/api/custom/cloudfoundry/v2/sso/start?serviceUuid=5786644c-cbe9-4f35-a4b2-b7184ce9d507","2018-03-14T12:17:35","succeeded"
"Northwest","mkillens","myHystrixService","p-circuit-breaker-dashboard","standard-legacy","managed","create","https://spring-cloud-service-broker.cfapps.io/dashboard/p-circuit-breaker-dashboard/00ccc5b7-617e-4328-aed0-3ac6b57eab88","2018-03-16T14:11:32","succeeded"
"Northwest","cphillipson","eureka-instance-prime","p-service-registry","standard-legacy","managed","create","https://spring-cloud-service-broker.cfapps.io/dashboard/p-service-registry/17ff265a-f17c-4263-9745-a8c10fe6aaa3","2017-06-19T15:47:54","succeeded"
"Northwest","mkillens","myDiscoveryService","p-service-registry","standard-legacy","managed","create","https://spring-cloud-service-broker.cfapps.io/dashboard/p-service-registry/10b24822-e36e-4174-8206-fca4beb52232","2018-03-16T14:10:35","succeeded"
"Northwest","cphillipson","config-server-instance-prime","p-config-server","standard-legacy","managed","update","https://spring-cloud-service-broker.cfapps.io/dashboard/p-config-server/3e9dcd90-f81a-4f5f-b9c9-9f2be57b8574","2017-06-20T13:12:54","succeeded"
...
```

#### Summary

Sample `service-inventory-summary.csv`

```
organization,total
Northwest,40
zoo-labs,1

service,total
cleardb,8
cloudamqp,5
p-config-server,5
p-service-registry,5
p-circuit-breaker-dashboard,3
mlab,2
rediscloud,2
app-autoscaler,2
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

Total services: 41
```

## Credits

Tip of the hat to those who've gone before...

* Baeldung [1](http://www.baeldung.com/spring-email), [2](http://www.baeldung.com/spring-events)
* [John Thompson](https://springframework.guru/spring-data-mongodb-with-reactive-mongodb/)
* [Josh Long](https://github.com/joshlong/cf-task-demo)
* [Mohit Sinha](https://github.com/mohitsinha/spring-boot-webflux-reactive-mongo)
* [Pas Apicella](http://theblasfrompas.blogspot.com/2017/03/run-spring-cloud-task-from-pivotal.html)
* [Robert Watkins](https://gist.github.com/twasink/3073710)
