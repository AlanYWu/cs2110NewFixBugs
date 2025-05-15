package model;

import java.awt.*;
import java.util.Random;

/**
 * The orange ghost in the game.
 */
public class Clyde extends Ghost {

    /**
     * Random object to randomize Clyde's target.
     */
    final private Random random;

    /**
     * Constructs Clyde, a ghost associated to the given `model` with orange and initial delay
     * of 8 seconds.
     */
    public Clyde(GameModel model, Random random) {
        super(model, Color.ORANGE, 8000);
        this.random = random;
    }

    @Override
    protected MazeGraph.MazeVertex target() {
        if (state() == GhostState.CHASE) {
            MazeGraph.MazeVertex pacCurrent = model.pacMann().nearestVertex();
            int xp = pacCurrent.loc().i();
            int yp = pacCurrent.loc().j();

            int xc = nearestVertex().loc().i();
            int yc = nearestVertex().loc().j();


            int dx = xp - xc;
            int dy = yp - yc;
            double dis = Math.sqrt(dx * dx + dy * dy);

            if (dis >= 10.0) {
                return model.blinky().target();
            } else {
                int i = random.nextInt(model.width());
                int j = random.nextInt(model.height());
                return model.graph().closestTo(i, j);
            }
        } else if (state() == GhostState.FLEE) {
//            return new MazeGraph.MazeVertex(new MazeGraph.IPair(2,model.height()-3));
            return model.graph().closestTo(model.width() - 3, model.height() - 3);
        }
        return currentEdge().src();
    }
}
