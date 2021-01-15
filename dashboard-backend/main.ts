import { connect } from "https://deno.land/x/redis/mod.ts";
import {
  WebSocket,
  WebSocketServer,
} from "https://deno.land/x/websocket@v0.0.5/mod.ts";

let webSockets: WebSocket[] = [];

const setupWebSockerServer = () => {
  console.info("DashboardWebSocketServer started.")
  const webSocketServer = new WebSocketServer(8081);
  webSocketServer.on("connection", function (webSocket: WebSocket) {
    webSockets.push(webSocket);
    console.info("DashboardWebSocketServer: Connection opened.");
    webSocket.on("close", function () {
      console.info("DashboardWebSocketServer: Connection closed.");
      webSockets = webSockets.filter(ws => ws !== webSocket);
    });
  });
};

//NotifyLeavingAreBoltSocket
const setupNotifyLeavingAreaWebSocketServer = () => {
  console.info("NotifyLeavingAreaSetupWebSocketServer started.")
  const wss = new WebSocketServer(8082);
  wss.on("connection", function (ws: WebSocket) {
    console.info("NotifyLeavingAreaSetupWebSocketServer: Connection opened.");
    ws.on("close", function () {
      console.info("NotifyLeavingAreaSetupWebSocketServer: Connection closed.");
    });
    ws.on("message", function (message: string) {
      console.info(`NotifyLeavingAreaSetupWebSocketServer: Message: ${message}.`);
      broadcast(JSON.stringify({type: "leaving", ...JSON.parse(message)}));
    });
  });
};

//NotifySpeedingBolt socket
const setupNotifySpeedingWebSocketServer = () => {
  console.info("NotifySpeedingSetupWebSocketServer started.");
  const wss = new WebSocketServer(8083);
  wss.on("connection", function (ws: WebSocket) {
    console.info("NotifySpeedingSetupWebSocketServer: Connection opened.");
    ws.on("close", function () {
      console.info("NotifySpeedingSetupWebSocketServer: Connection closed.");
    });
    ws.on("message", function (message: string) {
      console.info(`NotifySpeedingSetupWebSocketServer: Message: ${message}.`);
      broadcast(JSON.stringify({type: "speeding", ...JSON.parse(message)}));
    });
  });
};


const broadcast = (message: string) => {
  webSockets.forEach(webSocket => {
    try{
      if(!webSocket.isClosed)
        webSocket.send(message);
    }catch(e){
      console.error("WebSocket was already closed when trying to send.");
    }
  });
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
  console.info("RedisFetcher subscribes to redis.");
  for await (const { message } of sub.receive()) {
    const data = await redisClient.hgetall(message);
    //example data: ["average_speed", "13.343391",  "location", "116.400480, 39.904100", "overall_distance", "7.672450"]
    let dataMap: {[index: string]:string} = { "taxi": message };
    for (let i=0; i < data.length; i+=2) {
      const propName = data[i];
      const propValue = data[i+1];
      dataMap[propName] = propValue;
    }
    console.log(JSON.stringify(dataMap));
    broadcast(JSON.stringify({type: "moving", ...dataMap}));
  }
};

setupWebSockerServer();
setupNotifyLeavingAreaWebSocketServer();
setupNotifySpeedingWebSocketServer();
setupRedisFetcher();
