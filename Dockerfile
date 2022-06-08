FROM openjdk
COPY target/*.jar recast-bo-tableau-functional-mapping.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/recast-bo-tableau-functional-mapping.jar"]
