package de.tum.cit.fop.maze.objects.powerups;

import de.tum.cit.fop.maze.objects.Player;

public class Hpup extends Powerup {
    public Hpup(float x, float y) {
        super(x, y, "exit.png");
    }

    @Override
    public void update(Player player, float delta) {
        if(player.getBounds().overlaps(getBounds())) {
            player.setHp(player.getHp()+1);
        }
    }
}
