FROM maven:3.3.9-jdk-8
MAINTAINER Jesse Lopez <jesse@axiomdatascience.com>

# Copy app
WORKDIR /usr/src/app
COPY . /usr/src/app/ 

# Test
CMD ["mvn", "test"]
