package model;

import java.awt.*;

/**
 * The pink ghost in the game.
 */
public class Pinky extends Ghost {

    /**
     * Constructs Pinky, a ghost associated to the given `model` with color pink and initial delay
     * of 4 seconds.
     */
    public Pinky(GameModel model) {
        super(model, Color.pink, 4000);
    }

    @Override
    protected MazeGraph.MazeVertex target() {
        if (state() == GhostState.CHASE) {
            model.MazeGraph.MazeVertex current = model.pacMann().nearestVertex();
            int x = current.loc().i();
            int y = current.loc().j();

            MazeGraph.Direction dir = model.pacMann().currentEdge().direction();
            switch (dir) {
                case LEFT:
                    x -= 3;
                case RIGHT:
                    x += 3;
                case UP:
                    y += 3;
                case DOWN:
                    y -= 3;
            }
            return model.graph().closestTo(x, y);
        } else if (state() == GhostState.FLEE) {
            return model.graph().closestTo(model.width() - 3, 2);
        } else if (state() == GhostState.WAIT) {
            return model.pinky().location().nearestVertex();
        } else {
            System.out.println("something wrong");
            return model.graph().closestTo(model.width() - 3, 2);
        }
    }
}
