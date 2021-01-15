import L, {LatLng, Point} from 'leaflet';
import React, {useEffect, useState} from 'react';
import {MapContainer, Marker, Popup, TileLayer, useMapEvents} from 'react-leaflet';
import {ApplicationState, cacheEvent, processEvents} from './EventHandler';
import {calculateDistanceToBeijingCenter} from './CoordinateHelper';


export const App = () => {

  const [selectedTaxi, setSelectedTaxi] = useState<string | undefined>();
  const [state, setState] = useState<ApplicationState>({
    locations: {},
    speeding: [],
    leaving: [],
    overallDistances: {},
    bannedTaxis: []
  });
  const [error, setError] = useState<boolean>(false);

  useEffect(() => {
    const endpoint = 'ws://127.0.0.1:8081';
    const ws: WebSocket = new WebSocket(endpoint);
    ws.onerror = () => setError(true);
    ws.onopen = () => console.log('WebSocket connected.');
    ws.onmessage = (message: MessageEvent) => {
      if (!message) return;
      cacheEvent(JSON.parse(message.data));
    };
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      setState(processEvents(state));
    }, 1000);
    return () => {
      clearInterval(interval);
    };
  }, [state]);

  // @ts-ignore
  return (
    <div>
      <h1 style={{padding: "10px 20px", margin: 0, background: "#ffd93b"}}>Taxi Fleet Monitoring</h1>
      {error ? <h2 style={{background: 'red', color: 'white'}}>Could not connect to backend. Please retry!</h2> : null}
      <div style={{height: `calc(100vh - 63px)`, position: "relative"}}>

        <MapContainer
          center={[40, 116]}
          zoom={10}
          scrollWheelZoom={true}
          style={{height: '100%'}}
        >
          {selectedTaxi ? <FlyToLocation location={state.locations[selectedTaxi]} drag={() => setSelectedTaxi(undefined)}/> : null}
          <TileLayer
            attribution=''
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {Object.entries(state.locations).slice(0, 500).map(([taxi, location]) => (
            <Marker
              key={taxi}
              position={location}
              icon={L.icon({
                iconUrl: state.speeding.includes(taxi) ? 'taxi_fast.png' : 'taxi.png',
                iconAnchor: new Point(25, 35),
                iconSize: new Point(50, 50),
              })}
            >
              <Popup>
                Taxi: {taxi}<br/>
                Distance to center: {calculateDistanceToBeijingCenter(location[0], location[1])}
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      <div
        style={{
          display: 'flex',
          flexDirection: "column",
          position: "absolute",
          width: '100%',
          height: '100%',
          top: 0,
          left: 0,
          alignItems: "flex-start",
          justifyContent: "flex-end",
          zIndex: 1000,
          pointerEvents: "none"
        }}
      >
        <InfoBox>
          <InfoMetric label="Driving taxis">
            {Object.keys(state.locations).length}
          </InfoMetric>
          <InfoMetric label="Overall distance">
            {(Object.values(state.overallDistances)
              .reduce((p, c) => (p + c), 0)/1000)
              .toFixed(2)} km
          </InfoMetric>
        </InfoBox>
        <InfoBox>
          <InfoMetric label="Area violations">
            {state.leaving.map((taxi: string) =>
              <TaxiTag key={taxi} taxi={taxi} onClick={() => {
                setSelectedTaxi(taxi);
              }}/>
            )}
          </InfoMetric>
        </InfoBox>
        <InfoBox>
          <InfoMetric label="Speeding incidents">
            {state.speeding.map((taxi: string) =>
              <TaxiTag key={taxi} taxi={taxi} onClick={() => {
                setSelectedTaxi(taxi);
              }}/>
            )}
          </InfoMetric>
        </InfoBox>
      </div>
    </div>
    </div>
  );
};

const FlyToLocation = ({location: [latitude, longitude], drag}: { location: [latitude: number, longitude: number], drag: () => any }) => {
  const map = useMapEvents({drag});
  useEffect(() => {
    map.flyTo(new LatLng(latitude, longitude), 15);
  }, [latitude, longitude]);
  return null;
}

const InfoMetric = ({label, children}: {label: string, children?: any}) => <div>
  <div style={{textTransform: "uppercase", fontSize: 14}}>{label}</div>
  <div style={{fontWeight: "bold", fontSize: 26, marginBottom: 10}}>{children}</div>
</div>

const TaxiTag = ({taxi, onClick}: {taxi: string, onClick: () => any}) =>
  <div onClick={onClick} style={{ display: "inline-block", margin: 2, fontSize: 14, padding: "3px 5px", borderRadius: 10, background: "white", cursor: "pointer", boxShadow: "0px 4px 0px rgba(0,0,0,.2)",}}>Taxi {taxi}</div>

const InfoBox = ({children}: {children: any}) => <div style={{padding: 10, marginBottom: 20, marginLeft: 50,  background: "#ffd93b", width: "20%", pointerEvents: "all", maxHeight: 250, overflow: "auto", boxShadow: "0px 3px 3px 2px rgba(0,0,0,.2)", borderRadius: 10}}>
  {children}
</div>
