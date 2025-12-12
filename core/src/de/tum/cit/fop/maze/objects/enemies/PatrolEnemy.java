package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.Player;

public class PatrolEnemy extends Enemy {
    private final boolean axis; //where's it headed? x-axis or y-axis
    private float dir = 1; //direction of movement 1 for going right/up, -1 for going left/down

    public PatrolEnemy(int x, int y, boolean axis) {
        super(x, y, "assets/player.png");
        this.axis = axis;
        this.speed = 150;
    }

    @Override
    protected void move(float delta, Player player, GameMap map) {
        float dx = 0, dy = 0; //direction towards x and y respectively
        if(axis) {
            dx = dir * speed * delta; //if it's true (x-axis) move along x
        } else {
            dy = dir * speed * delta; //same for y
        }

        Rectangle pb = player.getBounds();
        Rectangle next = new Rectangle(x + dx, y + dy, w, h);

        if(!map.collidesWithWall(next) && !next.overlaps(pb)) { //move if it doesn't collide
            x += dx;
            y += dy;
        } else { //change direction
            dir *= -1f;
        }

        if(dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
    }
}
