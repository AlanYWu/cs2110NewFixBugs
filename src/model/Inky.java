package model;

import java.awt.*;

/**
 * The cyan ghost in the game.
 */
public class Inky extends Ghost {

    /**
     * Constructs Inky, a ghost associated to the given `model` with color cyan and initial delay
     * of 6 seconds.
     */
    public Inky(GameModel model) {
        super(model, Color.CYAN, 6000);
    }

    @Override
    protected MazeGraph.MazeVertex target() {
        if (state() == GhostState.CHASE) {
            model.MazeGraph.MazeVertex current = model.pacMann().nearestVertex();
            int x = current.loc().i();
            int y = current.loc().j();

            int xb = model.blinky().nearestVertex().loc().i();
            int yb = model.blinky().nearestVertex().loc().j();

            return model.graph().closestTo(2 * x - xb, 2 * y + yb);
        } else if (state() == GhostState.FLEE) {
            return model.graph().closestTo(2, model.height() - 3);
        }
        return currentEdge().src();
    }

}
