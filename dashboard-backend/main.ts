import { opine } from "https://deno.land/x/opine@0.26.0/mod.ts";
import { connect } from "https://deno.land/x/redis/mod.ts";
import {
  WebSocket,
  WebSocketServer,
} from "https://deno.land/x/websocket@v0.0.5/mod.ts";

let webSockets: WebSocket[] = [];

const setupWebSockerServer = () => {
  const webSocketServer = new WebSocketServer(8081);
  webSocketServer.on("connection", function (webSocket: WebSocket) {
    webSockets.push(webSocket);
    console.log("opened");
    webSocket.on("close", function () {
      console.log("closed");
      webSockets = webSockets.filter(ws => ws !== webSocket);
    });
    webSocket.on("message", function (message: string) {
      console.log(message);
      webSocket.send(message + " he said");
    });
  });
};

//NotifyLeavingAreBoltSocket
const notifyLeavingAreaSetupWebSockerServer = () => {
  console.log("Hello")
  const wss = new WebSocketServer(8082);
  console.log("webSocketServer created!")
  wss.on("connection", function (ws: WebSocket) {
    webSockets.push(ws);
    console.log("8082 opened");
    ws.on("close", function () {
      console.log("8082 closed");
      webSockets = webSockets.filter(ws => ws !== ws);
    });
    ws.on("message", function (message: string) {
      console.log(message);
      ws.send(message + " in 8082 he said");
    });
  });
};

//NotifySpeedingBolt socket
const notifySpeedingSetupWebSockerServer = () => {
  console.log("Hello")
  const wss = new WebSocketServer(8083);
  console.log("webSocketServer created!")
  wss.on("connection", function (ws: WebSocket) {
    webSockets.push(ws);
    console.log("8083 opened");
    ws.on("close", function () {
      console.log("8083 closed");
      webSockets = webSockets.filter(ws => ws !== ws);
    });
    ws.on("message", function (message: string) {
      console.log(message);
      ws.send(message + " in 8083 he said");
    });
  });
};




const broadcast = (message: string) => {
  webSockets.forEach(webSocket => webSocket.send(message));
};





const setupRedisFetcher = async () => {
  // Once a redis object subscribes to a channel, it cannot be used to perform other ops like get, hget, ..
  // therefore two redis clients are created
  const redisSub = await connect({
    hostname: "redis",
    port: 6379,
  });

  const redisClient = await connect({
    hostname: "redis",
    port: 6379,
  });

  // Subscribe to HSET events, published everytime a HSET command is performed (message == taxi id)
  const sub = await redisSub.subscribe("__keyevent@0__:hset");
  console.log("subscribed...");
  (async function() {
    for await (const { channel, message } of sub.receive()) {
      const data = await redisClient.hgetall(message);
      //example data: ["average_speed", "13.343391",  "location", "116.400480, 39.904100", "overall_distance", "7.672450"]
      let dataMap: {[index: string]:string} = { "taxi": message};
      for (let i=0; i < data.length; i+=2) {
        const propName = data[i];
        const propValue = data[i+1];
        dataMap[propName] = propValue;
      }

      broadcast(JSON.stringify(dataMap));

    }
  })();
};

const setupWebServer = () => {
  const app = opine();
  app.use((req, res) => {
    res.send("Hello World");
  });
  app.listen(8080);
};

console.log("Starting..");
setupWebSockerServer();

notifyLeavingAreaSetupWebSockerServer();
notifySpeedingSetupWebSockerServer();

setupWebServer();
setupRedisFetcher();
