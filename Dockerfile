FROM openjdk
COPY target/*.jar 
EXPOSE 8084
ENTRYPOINT ["java","-jar","/recast-bo-tableau-functional-mapping.jar"]
