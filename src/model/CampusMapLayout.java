package model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory campus guide layout. Coordinates are normalized from 0.0 to 1.0. */
public final class CampusMapLayout {
    public static final class Point {
        private final double x;
        private final double y;

        public Point(double x, double y) {
            this.x = clamp(x);
            this.y = clamp(y);
        }

        public double getX() { return x; }
        public double getY() { return y; }

        private static double clamp(double value) {
            return Math.max(0.12, Math.min(0.88, value));
        }
    }

    private final Map<String, Point> locations = new LinkedHashMap<>();

    public CampusMapLayout() {
        locations.put("STUDENT_ZONE", new Point(0.18, 0.24));
        locations.put("PERSONNEL_ZONE", new Point(0.80, 0.20));
        locations.put("VISITOR_ZONE", new Point(0.78, 0.78));
        locations.put("TWO_WHEELER_ZONE", new Point(0.18, 0.78));
        locations.put("MAIN_BUILDING", new Point(0.49, 0.39));
        locations.put("LIBRARY", new Point(0.34, 0.60));
        locations.put("GYMNASIUM", new Point(0.66, 0.60));
        locations.put("ADMIN_BUILDING", new Point(0.49, 0.16));
    }

    private CampusMapLayout(Map<String, Point> source) {
        locations.putAll(source);
    }

    public CampusMapLayout copy() {
        return new CampusMapLayout(locations);
    }

    public Point getLocation(String landmarkId) {
        return locations.get(landmarkId);
    }

    public Map<String, Point> getLocations() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(locations));
    }

    public boolean setLocation(String landmarkId, double x, double y) {
        if (!locations.containsKey(landmarkId)) return false;
        locations.put(landmarkId, new Point(x, y));
        return true;
    }
}
