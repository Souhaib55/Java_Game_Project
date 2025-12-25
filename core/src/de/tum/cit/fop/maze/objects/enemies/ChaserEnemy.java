package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.Player;

public class ChaserEnemy extends Enemy {
    private float dx = 0, dy = 0, cd = 0; //direction to x, y, and change of direction respectively

    public ChaserEnemy(int x, int y) {
        super(x, y, "player.png");
    }

    @Override
    protected void move(float delta, Player player, GameMap map) {
        //TODO: implement chasing player with a*
        if(inChaseRange(player)) {
            Vector2 dir = new Vector2(player.getX() - x, player.getY() - y); //move towards player
            if(dir.len2() > 0) {
                dir.nor();
                dx = dir.x;
                dy = dir.y;
            }
        } else {
            // wander randomly if player is outside of range
            cd += delta;
            if(cd > 2) {
                dx = (int) (Math.random() * 3) - 1; //0,1,2 w/0 the -1, -1,0,1 with, so it's left/stay/right
                dy = (int) (Math.random() * 3) - 1;
                Vector2 dir = new Vector2(dx, dy);
                if(dir.len2() > 0) {
                    dir.nor();
                }
                dx = dir.x;
                dy = dir.y;
                cd = 0;
            }
        }

        float sx = dx * speed * delta; //step toward x
        float sy = dy * speed * delta; //step toward y
        Rectangle pb = player.getBounds();
        Rectangle next = new Rectangle(x + sx, y + sy, w, h);
        // move if no collision, otherwise attempt axis aligned movement
        if(!map.collidesWithWall(next) && !next.overlaps(pb)) {
            x += sx;
            y += sy;
        } else {
            Rectangle tryX = new Rectangle(x + sx, y, w, h); //bounds for next x movement
            Rectangle tryY = new Rectangle(x, y + sy, w, h); //same for y

            if(!map.collidesWithWall(tryX) && !tryX.overlaps(pb)) {
                x += sx; //move along x if possible
            }
            if(!map.collidesWithWall(tryY) && !tryY.overlaps(pb)) {
                y += sy; //move along y if possible
            }
        }

        if(dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
    }

    private boolean inChaseRange(Player player) {
        return distance(player) <= 200; //chase range 200 pixels
    }
}