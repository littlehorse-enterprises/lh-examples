FROM node:alpine

COPY . /app
WORKDIR /app

RUN npm install --production

ENV PORT=3001
EXPOSE ${PORT}

ENTRYPOINT [ "npm" ]
CMD [ "start" ]
