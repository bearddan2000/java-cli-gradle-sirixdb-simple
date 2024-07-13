FROM gradle:jdk11

WORKDIR /app

ADD --chown=gradle:gradle /bin/ .

RUN chmod -R +x *

CMD ["gradle", "run"]
