FROM public.ecr.aws/amazoncorretto/amazoncorretto:21
RUN yum install -y shadow-utils
ARG USERNAME=app
ARG GROUPNAME=app
ARG UID=1000
ARG GID=1000

RUN groupadd -g $GID $GROUPNAME && \
    useradd -m -s /bin/bash -u $UID -g $GID $USERNAME
RUN chown -R $USERNAME:$GROUPNAME /tmp

USER $USERNAME
VOLUME [ "/tmp" ]
WORKDIR /home/$USERNAME/

COPY target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract

ENTRYPOINT [ "java" ]
CMD [ "-jar", "app/app.jar" ]
EXPOSE 8080
