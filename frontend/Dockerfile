FROM node:12.11.1-alpine AS build
ENV NODE_ENV production
WORKDIR /usr/src/app
RUN npm config set -g production false
COPY ["package.json", "package-lock.json*", "npm-shrinkwrap.json*", "./"]
RUN npm install
COPY . .
EXPOSE 8000
CMD ["npm", "run", "serve"]
