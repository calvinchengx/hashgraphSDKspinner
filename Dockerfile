FROM java:8

ADD ./sdk /usr/src/app/sdk

WORKDIR /usr/src/app/sdk

CMD ["java","-jar","swirlds.jar"]
