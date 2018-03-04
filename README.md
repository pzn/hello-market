hello-market
============
[![Build Status](https://travis-ci.org/pzn/hello-market.svg?branch=master)](https://travis-ci.org/pzn/hello-market)

what is it?
===========
A (dummy) web app integrated with AppDirect, using the [AppDistribution API](https://help.appdirect.com/appdistrib/appdistribution.html).

The webapp can recognize the following [subscription editions](./src/main/java/com/github/pzn/hellomarket/model/entity/SubscriptionType.java):
- TRIAL
- MONTHLY
- YEARLY
- LIFE

If you deploy it yourself, make sure to configure your AppDirect product with those editions!

live demo
=========
http://hellomarket.herokuapp.com

launch it locally?
==================
## prerequisites
- jdk8+
- postgresql 10.3+ (with database *hellomarket*)

```
mvn spring-boot:run \
  -Drun.jvmArguments="-Dappdirect.consumer_key='consumer_key' -Dappdirect.consumer_secret='consumer_secret'"
```

refer to [project's application.yml](./src/main/resources/application.yml) for default settings, or [override them](https://docs.spring.io/spring-boot/docs/1.5.9.RELEASE/reference/html/howto-properties-and-configuration.html#howto-use-short-command-line-arguments).

locally, but in docker
======================
because why not.

```
docker run --name postgres -p 5432:5432 \
           -e POSTGRES_USER=hellomarket \
           -e POSTGRES_PASSWORD=hellomarket \
           -e POSTGRES_DB=hellomarket \
           -d postgres:10.3-alpine
docker run --name hello-market -p 8080:8080 \
           --link postgres \
           -e spring.datasource.url="jdbc:postgresql://postgres:5432/hellomarket" \
           -e appdirect.consumer_key=YOUR_APPDIRECT_CONSUMER_KEY \
           -e appdirect.consumer_secret=YOUR_APPDIRECT_CONSUMER_SECRET \
           hello-market:0.0.1-SNAPSHOT
```

remotely, on heroku
===================

```
heroku apps:create hellomarket
heroku config:set APPDIRECT_CONSUMER_KEY="YOUR_APPDIRECT_CONSUMER_KEY"
heroku config:set APPDIRECT_CONSUMER_SECRET="YOUR_APPDIRECT_CONSUMER_SECRET"
heroku addons:create heroku-postgresql
heroku config:set MAVEN_CUSTOM_OPTS="-Ddockerfile.skip -DskipTests=true"
git push heroku master
```
