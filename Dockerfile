FROM openjdk
COPY target/*.jar 
EXPOSE 8761
ENTRYPOINT ["java","-jar","/recast-bo-tableau-functional-mapping.jar"]
