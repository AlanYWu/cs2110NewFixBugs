package model;

/**
 * Subclass responsible for PacMann's navigation through the player's inputs.
 */
public class PacMannManual extends PacMann {

    /**
     * Constructor for PacMannManual. Takes in the GameModel. Stored it into the field model.
     */
    public PacMannManual(GameModel model) {
        super(model);
    }

    /**
     * Check if there is an edge from PacMann’s nearest vertex in the direction of the most recent player command.
     * If there is, return that edge.
     * If there is not, check if there is an edge from PacMann’s nearest vertex in the same direction that he was just traveling.
     * If there is, return that edge.
     * If there is not, return null, which will signify that PacMann should not advance to a new edge.
     */
    @Override
    public MazeGraph.MazeEdge nextEdge() {
        MazeGraph.MazeVertex v = model.pacMann().nearestVertex();

        for (MazeGraph.MazeEdge edge : v.outgoingEdges()) {
            if (edge.direction().equals(model.playerCommand())) {
                return edge;
            }
        }

        for (MazeGraph.MazeEdge edge : v.outgoingEdges()) {
            if (edge.direction() == model.pacMann().currentEdge().direction()) {
                return edge;
            }
        }
        return null;
    }
}
