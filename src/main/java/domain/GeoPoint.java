package domain;

import lombok.Value;

@Value
public class GeoPoint {
    double latitude;
    double longitude;
    double altitude;

    /**
     * 두 지점 간의 대략적인 거리를 미터 단위로 계산 (Haversine formula)
     */
    public double distanceTo(GeoPoint other) {
        double R = 6371000; // 지구 반지름 (미터)
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
