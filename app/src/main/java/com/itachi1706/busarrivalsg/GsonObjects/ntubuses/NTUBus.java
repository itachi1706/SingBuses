package com.itachi1706.busarrivalsg.GsonObjects.ntubuses;

/**
 * Created by Kenneth on 7/9/2018.
 * for com.itachi1706.busarrivalsg.GsonObjects.ntubuses in SingBuses
 */
public class NTUBus {
    private Route[] routes;

    class Route {

        private MapRouting route = null;
        private Vehicles[] vehicles;
        private String name, routename, routenameraw, routethrough;
        private int id;
    }

    class Vehicles {
        private String nullable;
        // TODO: Populate this
    }

    class MapRouting {
        private MapPoints[] center;
        private int id;
        private MapNodes nodes;
    }

    class MapNodes {
        private int id;
        private float lat, lon;
        private String name, short_direction;
        private boolean is_stop_point;
        private MapPoints[] points;
    }

    class MapPoints {
        private float lat, lon;
    }
}
