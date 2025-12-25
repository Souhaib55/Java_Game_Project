package de.tum.cit.fop.maze.objects.powerups;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.*;

public abstract class Powerup extends GameObj {
    public Powerup(float x, float y, String path) {
        super(x, y, path);
    }

    public abstract void update(Player player, float delta);

    @Override
    public void render(SpriteBatch batch) {
        renderStaticObj(batch);
    }
    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
