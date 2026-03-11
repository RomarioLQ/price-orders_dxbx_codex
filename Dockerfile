FROM eclipse-temurin:21-alpine

# Настраиваем пользователя
ARG UNAME=dxbx
ARG UID=9999
ARG GID=9999
RUN addgroup -S -g $GID $UNAME && adduser -S -u $UID -G $UNAME $UNAME

# Копируем opentelemetry
COPY --chown=$UID:$GID opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Копируем JAR с нужными правами
ARG JAR_FILE
COPY --chown=$UID:$GID ${JAR_FILE} /app/application.jar

# Настраиваем часовой пояс
ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

USER $UID

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
EXPOSE 8087