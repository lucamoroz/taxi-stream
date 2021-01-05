import {opine} from 'https://deno.land/x/opine@0.26.0/mod.ts';
import {connect} from 'https://deno.land/x/redis/mod.ts';
import {
    WebSocket,
    WebSocketServer,
} from 'https://deno.land/x/websocket@v0.0.5/mod.ts';

let webSockets: WebSocket[] = [];

// WEBSOCKET HANDLER
const setupWebSockerServer = () => {
    const webSocketServer = new WebSocketServer(8081);
    webSocketServer.on('connection', function (webSocket: WebSocket) {
        webSockets.push(webSocket);
        webSocket.on('close', function () {
            webSockets = webSockets.filter(ws => ws !== webSocket);
        });
    });
};
const broadcast = (message: string) => {
    webSockets.forEach(webSocket => webSocket.send(message));
};

// DATA PROVIDER
const useDemoDataEmitterAndNotRedis = true;
const setupRedisFetcher = async () => {
    if (useDemoDataEmitterAndNotRedis) {
        const file = (await Deno.readTextFile('1.txt'))
            .split('\n')
            .map((line: string) => line.split(',').slice(2).reverse().join(', '));
        setInterval(() => broadcast(
            JSON.stringify([{taxi: 1, overall_distance: 2, location: file.shift()}])
        ), 1000);
    } else {
        const redis = await connect({
            hostname: 'redis',
            port: 6379,
        }).catch(() => console.error('Connection to redis could not be established!'));
        if (!redis) return;
        setInterval(async () => {
            const keys = await redis.keys('*');
            const values = await Promise.all(keys.map(async key => await redis.hgetall(key).then(value => ({
                taxi: key,
                ...Object.fromEntries(value.reduce((all: any, one: any, i) => {
                    const ch = Math.floor(i / 2);
                    all[ch] = [].concat((all[ch] || []), one);
                    return all;
                }, []))
            }))));
            broadcast(JSON.stringify(values));
        }, 1000);
    }
};

// STATIC WEBSERVER
// This can be used to access the webserver via a simple HTTP Request from SpeedingBolt and LeaveAreaBolt.
// This is not implemented yet.
const setupWebServer = () => {
    const app = opine();
    app.use((req, res) => {
        res.send('Hello World');
    });
    app.listen(8080);
};

setupWebSockerServer();
setupWebServer();
setupRedisFetcher();
console.log('Dashboard-Backend started!');
