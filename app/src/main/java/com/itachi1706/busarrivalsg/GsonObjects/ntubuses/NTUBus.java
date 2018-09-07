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
        private String nullable;
        // TODO: Populate this
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
