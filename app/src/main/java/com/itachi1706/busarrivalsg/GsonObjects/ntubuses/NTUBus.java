package com.itachi1706.busarrivalsg.GsonObjects.ntubuses;

/**
 * Created by Kenneth on 7/9/2018.
 * for com.itachi1706.busarrivalsg.GsonObjects.ntubuses in SingBuses
 */
public class NTUBus {
    private Route[] routes;

    public Route[] getRoutes() {
        return routes;
    }

    private double convertStringToDouble(String s) {
        return Double.parseDouble(s);
    }

    private long convertStringToLong(String s) {
        return Long.parseLong(s);
    }

    private float convertLocationString(String lonOrlat) {
        return Float.parseFloat(lonOrlat);
    }

    public class Route {

        private MapRouting route = null;
        private Vehicles[] vehicles;
        private String name, routename, routenameraw, routethrough;
        private int id;

        public MapRouting getRoute() {
            return route;
        }

        public Vehicles[] getVehicles() {
            return vehicles;
        }

        public String getName() {
            return name;
        }

        public String getRoutename() {
            return routename;
        }

        public String getRoutenameraw() {
            return routenameraw;
        }

        public String getRoutethrough() {
            return routethrough;
        }

        public int getId() {
            return id;
        }
    }

    public class Vehicles {
        private String device_ts, lat, lon, registration_code, speed, ts, license_no;
        private int bearing, routevariant_id, vehicle_id;
        private VehiclePosition position;
        private VehicleStats stats;

        public String getDevice_ts() {
            return device_ts;
        }

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }

        public String getRegistration_code() {
            return registration_code;
        }

        public String getSpeed() {
            return speed;
        }

        public String getTs() {
            return ts;
        }

        public String getLicense_no() {
            return license_no;
        }

        public int getBearing() {
            return bearing;
        }

        public int getRoutevariant_id() {
            return routevariant_id;
        }

        public int getVehicle_id() {
            return vehicle_id;
        }

        public VehiclePosition getPosition() {
            return position;
        }

        public VehicleStats getStats() {
            return stats;
        }
    }

    public class VehiclePosition {
        private int bearing, speed;
        private long device_ts, ts;
        private String lat, lon;

        public int getBearing() {
            return bearing;
        }

        public int getSpeed() {
            return speed;
        }

        public long getDevice_ts() {
            return device_ts;
        }

        public long getTs() {
            return ts;
        }

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }
    }

    public class VehicleStats {
        private String avg_speed, cumm_speed_10, cumm_speed_2, lat, lon;
        private int bearing, speed;
        private long device_ts, ts;

        public String getAvg_speed() {
            return avg_speed;
        }

        public String getCumm_speed_10() {
            return cumm_speed_10;
        }

        public String getCumm_speed_2() {
            return cumm_speed_2;
        }

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }

        public int getBearing() {
            return bearing;
        }

        public int getSpeed() {
            return speed;
        }

        public long getDevice_ts() {
            return device_ts;
        }

        public long getTs() {
            return ts;
        }
    }

    public class MapRouting {
        private MapPoints[] center;
        private int id;
        private MapNodes[] nodes;

        public MapPoints[] getCenter() {
            return center;
        }

        public int getId() {
            return id;
        }

        public MapNodes[] getNodes() {
            return nodes;
        }
    }

    public class MapNodes {
        private int id;
        private float lat, lon;
        private String name, short_direction;
        private boolean is_stop_point;
        private MapPoints[] points;

        public int getId() {
            return id;
        }

        public float getLat() {
            return lat;
        }

        public float getLon() {
            return lon;
        }

        public String getName() {
            return name;
        }

        public String getShort_direction() {
            return short_direction;
        }

        public boolean isIs_stop_point() {
            return is_stop_point;
        }

        public MapPoints[] getPoints() {
            return points;
        }
    }

    public class MapPoints {
        private float lat, lon;

        public float getLat() {
            return lat;
        }

        public float getLon() {
            return lon;
        }
    }
}
