package model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import model.MazeGraph.Direction;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.IPair;
import model.MazeGraph.MazeVertex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.GameMap;
import util.MazeGenerator.TileType;

public class MazeGraphTest {

    /* Note, to conform to the precondition of the `MazeGraph` constructor, make sure that any
     * TileType arrays that you construct contain a `PATH` tile at index [2][2] and represent a
     * single, orthogonally connected component of `PATH` tiles. */

    /**
     * Create a game map with tile types corresponding to the letters on each line of `template`.
     * 'w' = WALL, 'p' = PATH, and 'g' = GHOSTBOX.  The letters of `template` must form a rectangle.
     * Elevations will be a gradient from the top-left to the bottom-right corner with a horizontal
     * slope of 2 and a vertical slope of 1.
     */
    static GameMap createMap(String template) {
        Scanner lines = new Scanner(template);
        ArrayList<ArrayList<TileType>> lineLists = new ArrayList<>();

        while (lines.hasNextLine()) {
            ArrayList<TileType> lineList = new ArrayList<>();
            for (char c : lines.nextLine().toCharArray()) {
                switch (c) {
                    case 'w' -> lineList.add(TileType.WALL);
                    case 'p' -> lineList.add(TileType.PATH);
                    case 'g' -> lineList.add(TileType.GHOSTBOX);
                }
            }
            lineLists.add(lineList);
        }

        int height = lineLists.size();
        int width = lineLists.getFirst().size();

        TileType[][] types = new TileType[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                types[i][j] = lineLists.get(j).get(i);
            }
        }

        double[][] elevations = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                elevations[i][j] = (2.0 * i + j);
            }
        }
        return new GameMap(types, elevations);
    }

    @DisplayName("WHEN a GameMap with exactly one path tile in position [2][2] is passed into the "
            + "MazeGraph constructor, THEN a graph with one vertex is created.")
    @Test
    void testOnePathCell() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwpww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(1, vertices.size());
        assertTrue(vertices.containsKey(new IPair(2, 2)));
    }

    @DisplayName("WHEN a GameMap with exactly two horizontally adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsHorizontal() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwppw
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // graph contains two vertices with the correct locations
        assertEquals(2, vertices.size());
        IPair left = new IPair(2, 2);
        IPair right = new IPair(3, 2);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        // left vertex has one edge to the vertex to its right
        assertNull(vl.edgeInDirection(Direction.LEFT));
        assertNull(vl.edgeInDirection(Direction.UP));
        assertNull(vl.edgeInDirection(Direction.DOWN));
        MazeEdge l2r = vl.edgeInDirection(Direction.RIGHT);
        assertNotNull(l2r);

        // edge from left to right has the correct fields
        double lElev = map.elevations()[2][2];
        double rElev = map.elevations()[3][2];
        assertEquals(vl, l2r.src());
        assertEquals(vr, l2r.dst());
        assertEquals(Direction.RIGHT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

        // right vertex has one edge to the vertex to its left with the correct fields
        assertNull(vr.edgeInDirection(Direction.RIGHT));
        assertNull(vr.edgeInDirection(Direction.UP));
        assertNull(vr.edgeInDirection(Direction.DOWN));
        MazeEdge r2l = vr.edgeInDirection(Direction.LEFT);
        assertNotNull(r2l);
        assertEquals(vr, r2l.src());
        assertEquals(vl, r2l.dst());
        assertEquals(Direction.LEFT, r2l.direction());
        assertEquals(MazeGraph.edgeWeight(rElev, lElev), r2l.weight());
    }

    @DisplayName("WHEN a GameMap with exactly two vertically adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsVertical() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwpww
                wwpww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // Verify vertices
        assertEquals(2, vertices.size());
        IPair top = new IPair(2, 2);
        IPair bottom = new IPair(2, 3);
        assertTrue(vertices.containsKey(top));
        assertTrue(vertices.containsKey(bottom));

        MazeVertex vt = vertices.get(top);
        MazeVertex vb = vertices.get(bottom);

        // Top vertex has one edge downward
        assertNull(vt.edgeInDirection(Direction.LEFT));
        assertNull(vt.edgeInDirection(Direction.RIGHT));
        assertNull(vt.edgeInDirection(Direction.UP));
        MazeEdge t2b = vt.edgeInDirection(Direction.DOWN);
        assertNotNull(t2b);

        // Verify edge properties (top → bottom)
        double tElev = map.elevations()[2][2];
        double bElev = map.elevations()[2][3];
        assertEquals(vt, t2b.src());
        assertEquals(vb, t2b.dst());
        assertEquals(Direction.DOWN, t2b.direction());
        assertEquals(MazeGraph.edgeWeight(tElev, bElev), t2b.weight());

        // Bottom vertex has one edge upward
        assertNull(vb.edgeInDirection(Direction.LEFT));
        assertNull(vb.edgeInDirection(Direction.RIGHT));
        assertNull(vb.edgeInDirection(Direction.DOWN));
        MazeEdge b2t = vb.edgeInDirection(Direction.UP);
        assertNotNull(b2t);

        // Verify edge properties (bottom → top)
        assertEquals(vb, b2t.src());
        assertEquals(vt, b2t.dst());
        assertEquals(Direction.UP, b2t.direction());
        assertEquals(MazeGraph.edgeWeight(bElev, tElev), b2t.weight());
    }

    @DisplayName("WHEN a GameMap includes two path tiles in the first and last column of the same "
            + "row, THEN (tunnel) edges are created between these tiles with the correct properties.")
    @Test
    void testHorizontalTunnelEdgeCreation() {
        GameMap map = createMap("""
                wwwww
                wwwww
                pppwp
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // Verify vertices (tunnel endpoints)
        assertEquals(4, vertices.size());
        IPair left = new IPair(0, 2);
        IPair right = new IPair(4, 2);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        // Left vertex has a tunnel edge rightward
        assertNull(vl.edgeInDirection(Direction.UP));
        assertNull(vl.edgeInDirection(Direction.DOWN));
        MazeEdge l2r = vl.edgeInDirection(Direction.LEFT);
        assertNotNull(l2r);

        // Verify tunnel edge (left → right)
        double lElev = map.elevations()[0][2];
        double rElev = map.elevations()[4][2];
        assertEquals(vl, l2r.src());
        assertEquals(vr, l2r.dst());
        assertEquals(Direction.LEFT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

        // Right vertex has a tunnel edge leftward
        assertNull(vr.edgeInDirection(Direction.UP));
        assertNull(vr.edgeInDirection(Direction.DOWN));
        MazeEdge r2l = vr.edgeInDirection(Direction.RIGHT);
        assertNotNull(r2l);

        // Verify tunnel edge (right → left)
        assertEquals(vr, r2l.src());
        assertEquals(vl, r2l.dst());
        assertEquals(Direction.RIGHT, r2l.direction());
        assertEquals(MazeGraph.edgeWeight(rElev, lElev), r2l.weight());

    }

    @DisplayName("WHEN a GameMap includes a cyclic connected component of path tiles with a "
            + "non-path tiles in the middle, THEN its graph includes edges between all adjacent "
            + "pairs of vertices.")
    @Test
    void testCyclicPaths() {
        GameMap map = createMap("""
                wwwwwww
                wwwwwww
                wwpppww
                wwpwpww
                wwpppww
                wwwwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // Verify all 8 path tiles in the cycle
        assertEquals(8, vertices.size());
        IPair[] cyclePositions = {
                new IPair(2, 2), new IPair(3, 2), new IPair(4, 2),  // Top row
                new IPair(4, 3),                                     // Right side
                new IPair(4, 4), new IPair(3, 4), new IPair(2, 4),   // Bottom row
                new IPair(2, 3)                                      // Left side
        };
        for (IPair pos : cyclePositions) {
            assertTrue(vertices.containsKey(pos));
        }

        // Verify edges for a sample vertex (2,2)
        MazeVertex v = vertices.get(new IPair(2, 2));
        assertNotNull(v.edgeInDirection(Direction.RIGHT));  // → (3,2)
        assertNotNull(v.edgeInDirection(Direction.DOWN));   // → (2,3)
        assertNull(v.edgeInDirection(Direction.LEFT));
        assertNull(v.edgeInDirection(Direction.UP));
    }

    @DisplayName("WHEN a GameMap has an L-shaped path, THEN vertices are connected with correct edges.")
    @Test
    void testLShapedPath() {
        GameMap map = createMap("""
                wwwww
                wwpww
                wwppw
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(3, vertices.size());
        MazeVertex corner = vertices.get(new IPair(2, 2));
        assertNotNull(corner.edgeInDirection(Direction.RIGHT));  //  (3,2)
        assertNotNull(corner.edgeInDirection(Direction.UP));     //  (2,1)
    }

    @DisplayName("WHEN a GameMap has a Z-shaped path, THEN vertices are connected with correct edges.")
    @Test
    void testZShapedPath() {
        GameMap map = createMap("""
                wwppw
                wwpww
                wwpww
                wwppw""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // Verify all 5 expected vertices
        assertEquals(6, vertices.size());
        IPair[] pathPositions = {
                new IPair(2, 0), new IPair(3, 0),  // Top row
                new IPair(2, 1),                   // Middle vertical
                new IPair(2, 2),                   // Middle vertical
                new IPair(2, 3), new IPair(3, 3)   // Bottom row
        };
        for (IPair pos : pathPositions) {
            assertTrue(vertices.containsKey(pos));
        }

        // Test edges for key vertices
        MazeVertex topRight = vertices.get(new IPair(3, 0));
        MazeVertex middle = vertices.get(new IPair(2, 1));
        MazeVertex bottomLeft = vertices.get(new IPair(2, 3));

        // Top-right (3,0) connects leftward and upward
        assertNull(topRight.edgeInDirection(Direction.RIGHT));
        assertNotNull(topRight.edgeInDirection(Direction.UP)); // (3,3)
        assertNull(topRight.edgeInDirection(Direction.DOWN));
        assertNotNull(topRight.edgeInDirection(Direction.LEFT));  // (2,0)

        // Middle (2,1) connects upward and downward
        assertNull(middle.edgeInDirection(Direction.LEFT));
        assertNull(middle.edgeInDirection(Direction.RIGHT));
        assertNotNull(middle.edgeInDirection(Direction.UP));    // (2,0)
        assertNotNull(middle.edgeInDirection(Direction.DOWN));  // (2,2)

        // Bottom-left (2,3) connects rightward, upward, and downward
        assertNull(bottomLeft.edgeInDirection(Direction.LEFT));
        assertNotNull(bottomLeft.edgeInDirection(Direction.DOWN)); // (2,0)
        assertNotNull(bottomLeft.edgeInDirection(Direction.UP));    // (2,2)
        assertNotNull(bottomLeft.edgeInDirection(Direction.RIGHT)); // (3,3)
    }
}
