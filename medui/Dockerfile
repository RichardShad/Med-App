FROM theasp/clojurescript-nodejs:shadow-cljs-alpine as build
WORKDIR /app
COPY package.json package-lock.json shadow-cljs.edn /app/
RUN shadow-cljs npm-deps && npm install --save-dev shadow-cljs
COPY ./ /app/
ENV API_URL "https://rocky-beach-55793.herokuapp.com"
RUN shadow-cljs release app

FROM node:alpine
WORKDIR /app
RUN npm install --global http-server
ENV PORT=8080
EXPOSE $PORT
CMD npx http-server resources/public/ -p $PORT
COPY --from=build /app/ /app/
