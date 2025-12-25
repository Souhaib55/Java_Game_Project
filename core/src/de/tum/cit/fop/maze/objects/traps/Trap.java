package de.tum.cit.fop.maze.objects.traps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.*;

public abstract class Trap extends GameObj {
    public Trap(int x, int y, String path) {
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
