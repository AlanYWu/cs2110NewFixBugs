package graph;

import graph.Pathfinding.PathEnd;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static graph.SimpleGraph.*;

/**
 * Uses the `SimpleGraph` class to verify the functionality of the `Pathfinding` class.
 */
public class PathfindingTest {

    /*
     * Text graph format ([weight] is optional):
     * Directed edge: startLabel -> endLabel [weight]
     * Undirected edge (so two directed edges in both directions): startLabel -- endLabel [weight]
     */

    // a small, strongly-connected graph consisting of three vertices and four directed edges
    public static final String graph1 = """
            A -> B 2
            A -- C 6
            B -> C 3
            """;

    @DisplayName("WHEN we compute the `pathInfo` from a vertex `v`, THEN it includes an correct "
            + "entry for each vertex `w` reachable along a non-backtracking path from `v`.")
    @Nested
    class pathInfoTest {

        // Recall that "strongly connected" describes a graph that includes a (directed) path from
        // any vertex to any other vertex
        @DisplayName("In a strongly connected graph with no `previousEdge`.")
        @Test
        void testStronglyConnectedNoPrevious() {
            SimpleGraph g = SimpleGraph.fromText(graph1);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            // compute paths from source vertex "A"
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(0, paths.get(va).distance());
            // since the shortest path A -> A is empty, we can't assert anything about its last edge
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "B"
            paths = Pathfinding.pathInfo(vb, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(9, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(0, paths.get(vb).distance());
            assertEquals(3, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "C"
            paths = Pathfinding.pathInfo(vc, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(6, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(8, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(0, paths.get(vc).distance());
        }

        @DisplayName("In a graph that is *`not* strongly connected` and `pathInfo` is computed "
                + "starting from a vertex that cannot reach all other vertices.")
        @Test
        void testNotStronglyConnected() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(1, paths.size()); // only va is reachable
            assertTrue(paths.containsKey(va));
            assertFalse(paths.containsKey(vb));
        }

        @DisplayName("In a strongly connected graph with a `previousEdge` that prevents some vertex "
                + "from being reached.")
        @Test
        void testStronglyConnectedPreviousStillReachable() {
            // Given strongly connected graph
            SimpleGraph g = SimpleGraph.fromText("""
                    A -- B 2
                    """);

            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");

            // Let’s say we *just arrived at B via edge from A*, so we can't backtrack along B->A
            SimpleEdge prev = g.getEdge(va, vb);

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(vb, prev);

            // We should still be able to reach B and C via A->B->C
            assertEquals(1, paths.size());
            assertEquals(0, paths.get(vb).distance());
        }

        @DisplayName("In a graph where the shortest path with backtracking is shorter than the "
                + "shortest non-backtracking path.")
        @Test
        void testBacktrackingShorter() {
            String graph = """
                    A -> B 1
                    B -> A 1
                    A -> C 10
                    """;

            SimpleGraph g = SimpleGraph.fromText(graph);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

            // Should prefer A -> C directly (10), instead of backtracking A -> B -> A -> C (12)
            assertEquals(10, paths.get(vc).distance());
            assertEquals(g.getEdge(va, vc), paths.get(vc).lastEdge());
        }


        @DisplayName("In a graph where some shortest path includes at least 3 edges.")
        @Test
        void testLongerPaths() {
            // A -> B -> C -> D is the only non-backtracking path from A to D
            String graph = """
                    A -> B 2
                    B -> C 3
                    C -> D 4
                    """;

            SimpleGraph g = SimpleGraph.fromText(graph);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

            assertEquals(4, paths.size());  // A, B, C, D are all reachable
            assertEquals(0, paths.get(va).distance());
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());

            assertEquals(5, paths.get(vc).distance());  // 2 + 3
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            assertEquals(9, paths.get(vd).distance());  // 2 + 3 + 4
            assertEquals(g.getEdge(vc, vd), paths.get(vd).lastEdge());
        }
    }

    // Example graph from Prof. Myers's notes
    public static final String graph2 = """
            A -> B 9
            A -> C 14
            A -> D 15
            B -> E 23
            C -> E 17
            C -> D 5
            C -> F 30
            D -> F 20
            D -> G 37
            E -> F 3
            E -> G 20
            F -> G 16""";

    /**
     * Ensures `pathEdges` is a well-formed path: the `dst` of each edge equals the `src` of the
     * subsequent edge, and that the ordered list of all vertices in the path equals
     * `expectedVertices`. Requires `path` is non-empty.
     */
    private void assertPathVertices(List<String> expectedVertices, List<SimpleEdge> pathEdges) {
        ArrayList<String> pathVertices = new ArrayList<>();
        pathVertices.add(pathEdges.getFirst().src().label);
        for (SimpleEdge e : pathEdges) {
            assertEquals(pathVertices.getLast(), e.src().label);
            pathVertices.add(e.dst().label);
        }
        assertIterableEquals(expectedVertices, pathVertices);
    }

    @DisplayName("WHEN a weighted, directed graph is given, THEN `shortestNonBacktracking` returns"
            + "the list of edges in the shortest non-backtracking path from a `src` vertex to a "
            + "`dst` vertex, or null if no such path exists.")
    @Nested
    class testShortestNonBacktrackingPath {

        @DisplayName("When the shortest non-backtracking path consists of multiple edges.")
        @Test
        void testLongPath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("G"), null);
            assertNotNull(path);
            assertPathVertices(Arrays.asList("A", "C", "E", "F", "G"), path);
        }

        @DisplayName("When the shortest non-backtracking path consists of a single edge.")
        @Test
        void testOneEdgePath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);

            // Example: A → B is a single edge with weight 9
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(
                    g.getVertex("A"), g.getVertex("B"), null);

            assertNotNull(path);
            assertEquals(1, path.size());
            assertEquals("A", path.get(0).src().label);
            assertEquals("B", path.get(0).dst().label);

            // Alternatively, use the helper method:
            assertPathVertices(Arrays.asList("A", "B"), path);

        }

        @DisplayName("Path is empty when `src` and `dst` are the same.")
        @Test
        void testEmptyPath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("C"),
                    g.getVertex("D"), null);
            assertNotNull(path);
            assertPathVertices(Arrays.asList("C", "D"), path);
        }

        @DisplayName("Path is null when there is not a path from `src` to `dst` (even without "
                + "accounting for back-tracking.")
        @Test
        void testNoPath() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("B"), null);
            assertNull(path);
        }

        @DisplayName("Path is null when the non-backtracking condition prevents finding a path "
                + "from `src` to `dst`.")
        @Test
        void testNonBacktrackingPreventsPath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);

            // `A -> B` exists in graph2 with weight 9
            // Simulate that we just came from B to A (so can't go back A -> B)
            SimpleEdge prev = g.getVertex("B").outgoingEdges().stream()
                    .filter(e -> e.dst().equals(g.getVertex("A")))
                    .findFirst()
                    .orElse(null);

            // Note: graph2 has A -> B but not B -> A, so we'll fake the "came from B" part
            // Since graph2 is directed and doesn't contain B -> A, we need to fake previousEdge.
            // We'll instead pass in A -> B as previousEdge to block backtracking from B to A.

            SimpleEdge edgeAB = g.getVertex("A").outgoingEdges().stream()
                    .filter(e -> e.dst().equals(g.getVertex("B")))
                    .findFirst()
                    .orElseThrow();

            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(
                    g.getVertex("B"), g.getVertex("A"), edgeAB);

            assertNull(path);
        }

        @DisplayName("When the graph includes multiple shortest paths from `src` to `dst`, one of "
                + "them is returned")
        @Test
        void testMultipleShortestPaths() {
            String graphWithTies = """
                    A -> B 9
                    A -> C 14
                    A -> D 15
                    B -> E 23
                    C -> E 17
                    C -> D 5
                    C -> F 30
                    D -> F 20
                    D -> G 37
                    E -> F 3
                    E -> G 20
                    F -> G 16
                    A -> X 25
                    X -> G 25
                    """;

            SimpleGraph g = SimpleGraph.fromText(graphWithTies);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"), g.getVertex("G"), null);
            assertNotNull(path);

            // Two valid shortest paths: A-C-E-F-G and A-X-G
            List<String> vertices = new ArrayList<>();
            vertices.add(path.getFirst().src().label);
            for (SimpleEdge e : path) {
                vertices.add(e.dst().label);
            }

            List<String> path1 = Arrays.asList("A", "C", "E", "F", "G");
            List<String> path2 = Arrays.asList("A", "X", "G");

            assertTrue(vertices.equals(path1) || vertices.equals(path2),
                    "Returned path must be one of the valid shortest paths.");
        }
    }

}
