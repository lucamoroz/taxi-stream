from aiohttp import web
import json
import os
import random


async def handle(request):
    name = request.match_info.get('name', "Anonymous")
    text = "Hello, " + name
    return web.Response(text=text)


async def handle_color(request):
    """ returns a random color """
    colors = color_data["colors"]
    color = random.choice(colors)
    return web.json_response(data=color)


def load_colors(path: str):
    """ loads color data from path """
    with open(path) as f:
        data = f.read()
        return json.loads(data)


app = web.Application()
app.add_routes([
    web.get('/color', handle_color),
    web.get('/hello/{name}', handle)])

color_data = load_colors("/app/data.json")

if __name__ == '__main__':
    port = os.environ.get("HTTP_PORT", "4000")
    print("Starting server on port ", port)
    web.run_app(app, port=int(port))
