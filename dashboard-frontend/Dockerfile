# pull official base image
FROM node:13.12.0-alpine
RUN npm i -g serve

# set working directory
WORKDIR /app

# add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# install app dependencies
COPY package.json ./
COPY package-lock.json ./
RUN npm install --silent
#RUN npm install react-scripts@3.4.1 -g --silent

# add app
COPY . ./

# wait-for-it
RUN apk add bash curl
RUN curl https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh > wait-for-it.sh
RUN chmod +x ./wait-for-it.sh

# start app
RUN npm run build
CMD bash -c "./wait-for-it.sh -t 0  ${WAIT_FOR} -- serve -s build -l 8080"