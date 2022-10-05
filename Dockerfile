FROM public.ecr.aws/amazoncorretto/amazoncorretto:11
RUN yum install -y shadow-utils
ARG USERNAME=app
ARG GROUPNAME=app
ARG UID=1000
ARG GID=1000
RUN groupadd -g $GID $GROUPNAME && \
    useradd -m -s /bin/bash -u $UID -g $GID $USERNAME
USER $USERNAME
WORKDIR /home/$USERNAME/

COPY target/sample-bff-0.1.0-SNAPSHOT.jar /home/$USERNAME
CMD java -jar /home/app/sample-bff-0.1.0-SNAPSHOT.jar
