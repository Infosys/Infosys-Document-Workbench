# Provide base docker image having python
FROM base-python-mlmetrics:v3.6

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update -y
RUN apt-get install s3fs -y

RUN apt-get -y install tesseract-ocr
RUN apt-get -y install default-jdk

ENV JAVA_HOME /usr/lib/jvm/default-jdk/
ENV PATH $PATH:$JAVA_HOME/bin

ENV TESSERACT_PATH /usr/bin/tesseract
ENV FORMAT_CONVERTER_HOME /app/InfyFormatConverter

ENV APP_HOME /app
WORKDIR $APP_HOME
COPY . ./
COPY ../InfyFormatConverter ./
COPY ../lib ./

RUN pip install -r requirements.txt --index-url <Arifactory_URL> --trusted-host infyartifactory.ad.infosys.com

USER root

ENTRYPOINT ["/bin/bash"]

