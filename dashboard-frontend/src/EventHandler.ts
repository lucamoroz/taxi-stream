import {TaxiEvent} from './Events';
import {calculateDistanceToBeijingCenter} from './CoordinateHelper';

let cachedEvents: TaxiEvent[] = [];

export type Locations = Record<string, [number, number]>;
export type OverallDistances = {[taxi: string]: number};
export interface ApplicationState{
  locations: Locations,
  overallDistances: OverallDistances,
  averageSpeeds: Record<string, number>,
  leaving: string[],
  speeding: string[],
  bannedTaxis: string[]
}

export const cacheEvent = (event: TaxiEvent) => {
  cachedEvents.push(event);
}
export const processEvents = (immutableState: ApplicationState) => {
  let state = JSON.parse(JSON.stringify(immutableState));
  let processEvents = cachedEvents;
  cachedEvents = [];
  processEvents.forEach(event => processEvent(event, state));
  return state;
}

export const processEvent = (event: TaxiEvent, state: ApplicationState) => {
  if(state.bannedTaxis.includes(event.taxi)) return;

  if(event.type === 'moving'){
    if(event.location){
      const location = event.location.split(", ").map((v:string) => +v).reverse() as [number, number];
      const isFarAway = calculateDistanceToBeijingCenter(...location) > 15000;
      state.locations[event.taxi] = location;
      if(isFarAway && !state.bannedTaxis.includes(event.taxi)){
        state.bannedTaxis.push(event.taxi);
        delete state.locations[event.taxi];
        delete state.overallDistances[event.taxi];
        state.leaving = state.leaving.filter(taxi => taxi !== event.taxi);
        state.speeding = state.speeding.filter(taxi => taxi !== event.taxi);
      }else{
        state.bannedTaxis = state.bannedTaxis.filter(taxi => taxi !== event.taxi);
      }

      if (event.average_speed) {
        state.averageSpeeds[event.taxi] = +event.average_speed;
      }
    }
    if(event.overall_distance){
      state.overallDistances[event.taxi] = +event.overall_distance;
    }
  }else if(event.type === 'leaving'){
    state.leaving = [...state.leaving, event.taxi].filter(distinct).filter(taxi => event.leavingArea || (event.taxi !== taxi));
  }else if(event.type === 'speeding'){
    state.speeding = [...state.speeding, event.taxi].filter(distinct).filter(taxi => (event.speeding || (event.taxi !== taxi)));
  }
}
const distinct = (taxi: string, index: number, taxis: string[]) => taxis.indexOf(taxi) === index;