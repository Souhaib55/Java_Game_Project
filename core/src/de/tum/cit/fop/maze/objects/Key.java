package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Key extends GameObj {
    public Key(int x, int y) {
        super(x, y, "assets/key.png");
    }

    @Override
    public void render(SpriteBatch batch) {
        renderStaticObj(batch);
    }

    @Override
    public Rectangle getBounds() {
        return getBoundsStaticObj();
    }
}
