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


const notifySetupWebSockerServer = () => {
  const webSocketServer = new WebSocketServer(8082);
  webSocketServer.on("connection", function (webSocket: WebSocket) {
    webSockets.push(webSocket);
    console.log("8082 opened");
    webSocket.on("8082 close", function () {
      console.log("8082 closed");
      webSockets = webSockets.filter(ws => ws !== webSocket);
    });
    webSocket.on("8082 message", function (message: string) {
      console.log(message);
      webSocket.send(message + " in 8082 he said");
    });
  });
};




const broadcast = (message: string) => {
  webSockets.forEach(webSocket => webSocket.send(message));
};





const setupRedisFetcher = async () => {
  const redis = await connect({
    hostname: "redis",
    port: 6379,
  });
  const file = (await Deno.readTextFile("1.txt"))
    .split("\n")
    .map((line: string) => line.split(",").slice(2).reverse().join(", "));
  setInterval(async () => {
    /*const keys = await redis.keys('*');
        const values = await Promise.all(keys.map(async key => await redis.hgetall(key).then(value => ({
            taxi: key,
            ...Object.fromEntries(value.reduce((all: any, one: any, i) => {
                const ch = Math.floor(i / 2);
                all[ch] = [].concat((all[ch] || []), one);
                return all;
            }, []))
        }))));
        broadcast(JSON.stringify(values));*/
    broadcast(
      JSON.stringify([{ taxi: 1, overall_distance: 2, location: file.shift() }])
    );
  }, 1000);
};

const setupWebServer = () => {
  const app = opine();
  app.use((req, res) => {
    res.send("Hello World");
  });
  app.listen(8080);
};

console.log("Starting..");
// setupWebSockerServer();


notifySetupWebSockerServer();

setupWebServer();
setupRedisFetcher();