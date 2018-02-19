FROM clojure
COPY target/klusterz-example-0.1.0-SNAPSHOT-standalone.jar /usr/src/app/klusterz-example.jar
WORKDIR /usr/src/app/
CMD ["java", "-jar", "/usr/src/app/klusterz-example.jar"]