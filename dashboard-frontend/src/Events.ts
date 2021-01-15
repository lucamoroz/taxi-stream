export type TaxiEvent = MovingEvent | SpeedingEvent | LeavingEvent;

export interface MovingEvent{
  taxi: string;
  location?: string;
  overall_distance?: string;
  type: "moving";
}
export interface SpeedingEvent{
  taxi: string;
  speeding: boolean;
  type: "speeding";
}
export interface LeavingEvent{
  taxi: string;
  leavingArea: boolean;
  type: "leaving";
}