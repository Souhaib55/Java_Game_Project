package de.tum.cit.fop.maze.objects.powerups;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.objects.Player;

public class Speedup extends Powerup {

    public Speedup(float x, float y) {
        super(x, y, "speedup.png");
    }

    @Override
    public void update(Player player, float delta) {
        if(player.getBounds().overlaps(getBounds())) {
            player.speedEffect(1.5f, 2.5f, Color.GREEN); // 1.5x speed for 2.5s
        }
    }

}
