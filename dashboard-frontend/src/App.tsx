import L, { Point } from "leaflet";
import React, { useEffect, useState } from "react";
import { MapContainer, Marker, Popup, TileLayer } from "react-leaflet";

export const App = () => {
  const [data, setData] = useState<{
    totalTaxis: number;
    overallDistance: number;
    locations: Record<string, [number, number]>;
  }>();
  useEffect(() => {
    const endpoint = "ws://127.0.0.1:8081";
    const ws: WebSocket = new WebSocket(endpoint);
    ws.onopen = () => {
      console.log("ws connected!");
    };
    ws.onmessage = (message: MessageEvent) => {
      if (!message) return;
      const datas: {
        taxi: string;
        location: string;
        overall_distance: string;
      }[] = JSON.parse(message.data);
      setData({
        totalTaxis: datas.length,
        overallDistance: datas.reduce((p, c) => p + +c.overall_distance, 0),
        locations: Object.fromEntries(
          datas.map(d => [
            d.taxi,
            d.location.split(", ").map(v => +v) as [number, number],
          ])
        ),
      });
    };
  }, []);

  return (
    <div style={{ padding: "0 20px" }}>
      <h1>Taxi Fleet Monitoring</h1>
      {data ? (
        <MapContainer
          center={data.locations[1]}
          zoom={10}
          scrollWheelZoom={false}
          style={{ height: 400 }}
        >
          <TileLayer
            attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {Object.entries(data.locations).map(([taxi, location]) => (
            <Marker
              key={taxi}
              position={location}
              icon={L.icon({
                iconUrl: "taxi.png",
                iconAnchor: new Point(25, 35),
                iconSize: new Point(50, 50),
              })}
            >
              <Popup>Taxi: {taxi}</Popup>
            </Marker>
          ))}
        </MapContainer>
      ) : null}
      <div
        style={{
          display: "flex",
          width: "100%",
          justifyContent: "space-between",
        }}
      >
        <div>
          <h3>Currently driving taxis</h3>
          {data?.totalTaxis}
          <h3>Overall distance of all taxis</h3>
          {data?.overallDistance}
        </div>
        <div>
          <h3>Area violations</h3>
        </div>
        <div>
          <h3>Speeding incidents</h3>
        </div>
      </div>
    </div>
  );
};
