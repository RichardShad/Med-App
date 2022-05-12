FROM clojure:lein
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY . /usr/src/app/
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
ENV PORT 3000
ENV CLIENT_URL "https://hoppscotch.io"

ENV DB_NAME "medb"
ENV DB_USER "medadmin"
ENV DB_PASSWORD "123123"

EXPOSE $PORT
CMD java -jar app-standalone.jar
