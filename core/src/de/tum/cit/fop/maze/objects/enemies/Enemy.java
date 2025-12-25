package de.tum.cit.fop.maze.objects.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.GameMap;
import de.tum.cit.fop.maze.objects.GameObj;
import de.tum.cit.fop.maze.objects.Player;

public abstract class Enemy extends GameObj {
    //speed in pixels per second
    //attack timer times the attack to only attack in attackInterval time
    //attack range in pixels
    protected float speed = 75, timer = 0, range = TILE * 1.5f;
    protected final float interval = 1; //attack once per second
    protected int hp = 3;
    protected boolean alive = true;
    protected float lookX, lookY;

    protected final TextureRegion right, left, up, down;
    protected TextureRegion current;

    public Enemy(int x, int y, String path) {
        super(x * TILE, y * TILE, path);
        this.w = TILE;
        this.h = TILE;

        int fw = texture.getWidth() / 4;  // 4 columns
        int fh = texture.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(texture, fw, fh);

        right = tmp[0][0];
        left = tmp[0][1];
        up = tmp[0][2];
        down = tmp[0][3];

        current = right;
    }

    protected abstract void move(float delta, Player player, GameMap map);

    public void update(float delta, Player player, GameMap map) {
        if(!alive) {
            return;
        }

        if(Math.abs(lookY) > Math.abs(lookX)) {
            current = (lookY > 0) ? up : down;
        } else {
            current = (lookX > 0) ? right : left;
        }

        move(delta, player, map);

        if(inRange(player)) {
            if(timer <= 0) {
                player.loseLife();
                player.setTint(Color.RED);
                timer = interval;
            } else {
                timer -= delta;
            }
        } else {
            timer = 0;
        }
    }

    public boolean inRange(Player player) {
        return distance(player) <= range;
    }

    protected float distance(Player player) {
        return (float) Math.sqrt(Math.pow((player.getX() - x), 2) + Math.pow((player.getY() - y), 2)); //distance between two points
    }

    public void takeDamage(int dmg) {
        if(!alive) {
            return;
        }
        hp -= dmg;
        if(hp <= 0) {
            alive = false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public void render(SpriteBatch batch) {
        renderMovingObj(batch, current);
    }

    @Override
    public Rectangle getBounds() {
        return getBoundsMovingObj();
    }
}
