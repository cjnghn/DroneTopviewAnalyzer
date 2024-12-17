package calculator;

import domain.GeoPoint;
import domain.GeoreferencedObject;
import domain.TrajectoryIntersection;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class IntersectionCalculator {

    private static final double BOUNDARY_MARGIN = 0.0001; // 경계 상자에 약간의 여유 추가

    private static class LineSegment {
        GeoreferencedObject start;
        GeoreferencedObject end;
        double minLat, maxLat;
        double minLon, maxLon;
        String trajectoryId;
        double startTime, endTime;

        LineSegment(GeoreferencedObject start, GeoreferencedObject end, String trajectoryId) {
            this.start = start;
            this.end = end;
            this.trajectoryId = trajectoryId;
            this.startTime = start.getTrackedObject().getTimestamp();
            this.endTime = end.getTrackedObject().getTimestamp();

            // 경계 상자 계산 (여유 마진 추가)
            minLat = Math.min(start.getLocation().getLatitude(), end.getLocation().getLatitude()) - BOUNDARY_MARGIN;
            maxLat = Math.max(start.getLocation().getLatitude(), end.getLocation().getLatitude()) + BOUNDARY_MARGIN;
            minLon = Math.min(start.getLocation().getLongitude(), end.getLocation().getLongitude()) - BOUNDARY_MARGIN;
            maxLon = Math.max(start.getLocation().getLongitude(), end.getLocation().getLongitude()) + BOUNDARY_MARGIN;
        }

        boolean overlaps(LineSegment other) {
            // 시간 범위 체크 추가
            if (this.endTime < other.startTime || other.endTime < this.startTime) {
                return false;
            }
            return !(maxLat < other.minLat || minLat > other.maxLat ||
                    maxLon < other.minLon || minLon > other.maxLon);
        }

        // 선분의 중심점 계산
        double getCenterLat() {
            return (minLat + maxLat) / 2;
        }

        double getCenterLon() {
            return (minLon + maxLon) / 2;
        }
    }

    private static class KDNode {
        LineSegment segment;
        KDNode left, right;
        int depth;

        KDNode(LineSegment segment, int depth) {
            this.segment = segment;
            this.depth = depth;
        }
    }

    private static class IntersectionResult {
        GeoPoint point;
        double timestamp;

        IntersectionResult(GeoPoint point, double timestamp) {
            this.point = point;
            this.timestamp = timestamp;
        }
    }

    public List<TrajectoryIntersection> findIntersections(List<GeoreferencedObject> objects) {
        System.out.println("Starting intersection calculation...");
        System.out.println("Total objects: " + objects.size());

        Map<String, List<GeoreferencedObject>> trajectoryGroups = groupByTrackingId(objects);
        System.out.println("Number of trajectories: " + trajectoryGroups.size());

        // 선분들을 미리 생성하고 시간순 정렬
        List<LineSegment> allSegments = new ArrayList<>();
        for (Map.Entry<String, List<GeoreferencedObject>> entry : trajectoryGroups.entrySet()) {
            List<GeoreferencedObject> trajectory = entry.getValue();
            for (int i = 0; i < trajectory.size() - 1; i++) {
                allSegments.add(new LineSegment(trajectory.get(i), trajectory.get(i + 1), entry.getKey()));
            }
        }

        System.out.println("Total line segments created: " + allSegments.size());

        // 시간순 정렬
        allSegments.sort(Comparator.comparingDouble(s -> s.startTime));

        System.out.println("Building KD-tree...");
        KDNode root = buildKDTree(allSegments, 0);
        System.out.println("KD-tree built successfully");

        System.out.println("Finding intersections...");
        Set<String> processedPairs = new HashSet<>();
        List<TrajectoryIntersection> intersections = new ArrayList<>();
        int processedSegments = 0;

        for (LineSegment segment : allSegments) {
            List<LineSegment> candidates = new ArrayList<>();
            searchKDTree(root, segment, candidates);

            for (LineSegment candidate : candidates) {
                if (segment.trajectoryId.equals(candidate.trajectoryId)) continue;

                String pairKey = createPairKey(segment, candidate);
                if (processedPairs.contains(pairKey)) continue;
                processedPairs.add(pairKey);

                Optional<IntersectionResult> result = findSegmentIntersection(
                    segment.start, segment.end,
                    candidate.start, candidate.end
                );

                result.ifPresent(intersection -> intersections.add(
                    TrajectoryIntersection.builder()
                        .intersectionPoint(intersection.point)
                        .timestamp(intersection.timestamp)
                        .object1(segment.start)
                        .object2(candidate.start)
                        .build()
                ));
            }

            processedSegments++;
            if (processedSegments % 1000 == 0) {
                System.out.printf("Progress: %.1f%% (%d/%d)%n",
                        (processedSegments * 100.0) / allSegments.size(),
                        processedSegments, allSegments.size());
            }
        }

        System.out.println("Intersection calculation completed");
        System.out.println("Found " + intersections.size() + " intersections");

        return intersections;
    }

    private Map<String, List<GeoreferencedObject>> groupByTrackingId(List<GeoreferencedObject> objects) {
        Map<String, List<GeoreferencedObject>> groups = new HashMap<>();

        for (GeoreferencedObject obj : objects) {
            String trackingId = obj.getTrackedObject().getTrackingId();
            groups.computeIfAbsent(trackingId, k -> new ArrayList<>()).add(obj);
        }

        // 각 그룹 내에서 시간순 정렬
        for (List<GeoreferencedObject> group : groups.values()) {
            group.sort(Comparator.comparingDouble(o -> o.getTrackedObject().getTimestamp()));
        }

        return groups;
    }

    private KDNode buildKDTree(List<LineSegment> segments, int depth) {
        if (segments.isEmpty()) return null;

        int axis = depth % 2;
        // 중심점 기준으로 정렬
        segments.sort((a, b) -> {
            if (axis == 0) return Double.compare(a.getCenterLat(), b.getCenterLat());
            else return Double.compare(a.getCenterLon(), b.getCenterLon());
        });

        int median = segments.size() / 2;
        KDNode node = new KDNode(segments.get(median), depth);

        node.left = buildKDTree(new ArrayList<>(segments.subList(0, median)), depth + 1);
        node.right = buildKDTree(new ArrayList<>(segments.subList(median + 1, segments.size())), depth + 1);

        return node;
    }

    private void searchKDTree(KDNode node, LineSegment target, List<LineSegment> candidates) {
        if (node == null) return;

        if (node.segment.overlaps(target)) {
            candidates.add(node.segment);
        }

        int axis = node.depth % 2;
        double nodeCenter = axis == 0 ? node.segment.getCenterLat() : node.segment.getCenterLon();
        double targetMin = axis == 0 ? target.minLat : target.minLon;
        double targetMax = axis == 0 ? target.maxLat : target.maxLon;

        if (targetMin <= nodeCenter) {
            searchKDTree(node.left, target, candidates);
        }
        if (targetMax >= nodeCenter) {
            searchKDTree(node.right, target, candidates);
        }
    }

    private String createPairKey(LineSegment s1, LineSegment s2) {
        String id1 = s1.trajectoryId;
        String id2 = s2.trajectoryId;
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    private Optional<IntersectionResult> findSegmentIntersection(
        GeoreferencedObject start1, GeoreferencedObject end1,
        GeoreferencedObject start2, GeoreferencedObject end2) {

        GeoPoint p1 = start1.getLocation();
        GeoPoint p2 = end1.getLocation();
        GeoPoint p3 = start2.getLocation();
        GeoPoint p4 = end2.getLocation();

        // 선분 교차 판정을 위한 CCW(Counter Clock Wise) 계산
        double ccw1 = ccw(p1, p2, p3);
        double ccw2 = ccw(p1, p2, p4);
        double ccw3 = ccw(p3, p4, p1);
        double ccw4 = ccw(p3, p4, p2);

        // 두 선분이 교차하는 경우
        if (ccw1 * ccw2 < 0 && ccw3 * ccw4 < 0) {
            // 교차점 계산
            GeoPoint intersection = calculateIntersectionPoint(p1, p2, p3, p4);

            // 교차 시점의 timestamp 추정 (선형 보간)
            double ratio = calculateIntersectionRatio(p1, p2, intersection);
            double timestamp = lerp(
                start1.getTrackedObject().getTimestamp(),
                end1.getTrackedObject().getTimestamp(),
                ratio
            );

            return Optional.of(new IntersectionResult(intersection, timestamp));
        }

        return Optional.empty();
    }

    private double ccw(GeoPoint p1, GeoPoint p2, GeoPoint p3) {
        return (p2.getLongitude() - p1.getLongitude()) * (p3.getLatitude() - p1.getLatitude()) -
                (p3.getLongitude() - p1.getLongitude()) * (p2.getLatitude() - p1.getLatitude());
    }

    private GeoPoint calculateIntersectionPoint(GeoPoint p1, GeoPoint p2, GeoPoint p3, GeoPoint p4) {
        double px = ((p1.getLongitude() * p2.getLatitude() - p1.getLatitude() * p2.getLongitude()) *
                (p3.getLongitude() - p4.getLongitude()) -
                (p1.getLongitude() - p2.getLongitude()) *
                        (p3.getLongitude() * p4.getLatitude() - p3.getLatitude() * p4.getLongitude())) /
                ((p1.getLongitude() - p2.getLongitude()) * (p3.getLatitude() - p4.getLatitude()) -
                        (p1.getLatitude() - p2.getLatitude()) * (p3.getLongitude() - p4.getLongitude()));

        double py = ((p1.getLongitude() * p2.getLatitude() - p1.getLatitude() * p2.getLongitude()) *
                (p3.getLatitude() - p4.getLatitude()) -
                (p1.getLatitude() - p2.getLatitude()) *
                        (p3.getLongitude() * p4.getLatitude() - p3.getLatitude() * p4.getLongitude())) /
                ((p1.getLongitude() - p2.getLongitude()) * (p3.getLatitude() - p4.getLatitude()) -
                        (p1.getLatitude() - p2.getLatitude()) * (p3.getLongitude() - p4.getLongitude()));

        // 고도는 두 선분의 중간값으로 보간
        double pz = (p1.getAltitude() + p2.getAltitude() + p3.getAltitude() + p4.getAltitude()) / 4;

        return new GeoPoint(py, px, pz);  // latitude, longitude, altitude 순
    }

    private double calculateIntersectionRatio(GeoPoint start, GeoPoint end, GeoPoint intersection) {
        double dx = end.getLongitude() - start.getLongitude();
        double dy = end.getLatitude() - start.getLatitude();

        if (Math.abs(dx) > Math.abs(dy)) {
            return (intersection.getLongitude() - start.getLongitude()) / dx;
        } else {
            return (intersection.getLatitude() - start.getLatitude()) / dy;
        }
    }

    private double lerp(double start, double end, double ratio) {
        return start + (end - start) * ratio;
    }
}