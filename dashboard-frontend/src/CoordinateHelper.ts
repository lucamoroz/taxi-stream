export const EARTH_RADIUS = 6371000;
export const LAT_BEIJING = 39.916668;
export const LONG_BEIJING = 116.383331;

export const calculateDistance = (lat1: number, long1: number, lat2: number, long2: number) => {
  let phi1 = (lat1 / 360) * 2 * Math.PI;
  let phi2 = (lat2 / 360) * 2 * Math.PI;
  let lambda1 = (long1 / 360) * 2 * Math.PI;
  let lambda2 = (long2 / 360) * 2 * Math.PI;

  return 2 * EARTH_RADIUS * Math.asin(
    Math.sqrt(Math.pow(Math.sin((Math.abs(phi2 - phi1))/2), 2) +
      Math.cos(phi1) * Math.cos(phi2)*Math.pow(Math.sin((Math.abs(lambda2 - lambda1))/2),2)));
}

export const calculateDistanceToBeijingCenter = (lat1: number, long1: number) => {
  return calculateDistance(lat1, long1, LAT_BEIJING, LONG_BEIJING);
}