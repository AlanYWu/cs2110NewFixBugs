package model;

import java.awt.*;
import java.util.List;

/**
 * The red ghost in the game.
 */
public class Blinky extends Ghost{

    /**
     * Constructs Blinky, a ghost associated to the given `model` with color red and initial delay
     * of 2 seconds.
     */
    public Blinky(GameModel model) {
        super(model, Color.red, 2000);
    }

    @Override
    protected MazeGraph.MazeVertex target() {
        if (state() == GhostState.CHASE){
            return model.pacMann().nearestVertex();
        } else if (state() == GhostState.FLEE) {
            return model.graph().closestTo(2,2);
        }
//        double i = this.getBoundingBoxUL().i();
//        double j = this.getBoundingBoxUL().j();
//        return new MazeGraph.MazeVertex(new MazeGraph.IPair((int)i,(int)j));
        return currentEdge().src();
    }
}
