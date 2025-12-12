package de.tum.cit.fop.maze.objects.traps;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.objects.Player;

public class SludgeTrap extends Trap {
    private final float mult;

    public SludgeTrap(int x, int y, float mult) {
        super(x, y, "assets/key.png");
        this.mult = mult;
    }

    @Override
    public void update(Player player, float delta) {
        if(player.getBounds().overlaps(getBounds())) {
            player.speedEffect(mult, 0.1f, Color.BLUE);
        }
    }
}
