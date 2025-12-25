package de.tum.cit.fop.maze.objects.traps;

import de.tum.cit.fop.maze.objects.Player;

public class DamageTrap extends Trap {
    private final float interval;
    private float timer = 0;
    private boolean active = false;

    public DamageTrap(int x, int y, float interval) {
        super(x, y, "trap.png");
        this.interval = interval;
    }

    @Override
    public void update(Player player, float delta) {
        if(player.getBounds().overlaps(getBounds())) {
            if(!active) {
                player.loseLife();
                active = true;
                timer = 0f;
            }
            timer += delta;
            if(timer >= interval) {
                player.loseLife();
                timer = 0f;
            }
        } else {
            active = false;
            timer = 0f;
        }
    }
}
