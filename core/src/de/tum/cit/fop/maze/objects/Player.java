package de.tum.cit.fop.maze.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.objects.enemies.*;

import java.util.List;

public class Player extends GameObj {
    private int hp = 5, keys = 0;
    private float tintTimer = 0, attackTimer = 0, effectTimer = 0;
    private float lookX, lookY;
    private float speed = 1;
    private Color tint = Color.WHITE;

    private final TextureRegion right, left, up, down;
    private TextureRegion current;

    public Player(float x, float y) {
        super(x, y, "assets/player.png");
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

    public void move(float dx, float dy) {
        x += dx;
        y += dy;
        if(dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            lookX = dx / len;
            lookY = dy / len;
        }
    }

    public void update(float delta) {
        if(Math.abs(lookY) > Math.abs(lookX)) {
            current = (lookY > 0) ? up : down;
        } else {
            current = (lookX > 0) ? right : left;
        }

        if(attackTimer > 0f) {
            attackTimer -= delta;
        }

        if(effectTimer > 0) {
            effectTimer -= delta;
            if(effectTimer <= 0) {
                speed = 1f;
                setTint(tint);
            }
        }

        if(!tint.equals(Color.WHITE)) {
            tintTimer += delta;
            // Tint player for 0.25 seconds.
            if(tintTimer >= 0.25f) {
                tint = Color.WHITE;
                tintTimer = 0f;
            }
        }
    }

    public void loseLife(int n) {
        hp -= n;
        setTint(Color.RED);
    }

    public void loseLife() {
        loseLife(1);
    }

    public void attack(List<Enemy> e) {
        if(attackTimer > 0f) {
            return;
        }

        float range = Math.max(w, h) * 1.5f;
        float cx = x + w / 2f;
        float cy = y + h / 2f;

        boolean hit = false;
        float cosHalfAngle = (float) Math.cos(Math.toRadians(90)); //half angle for cone, so 180 degrees damage radius

        for(Enemy enemy : e) {
            if(!enemy.isAlive()) {
                continue;
            }

            Rectangle eb = enemy.getBounds();
            float ex = eb.x + eb.width / 2;
            float ey = eb.y + eb.height / 2;

            float vx = ex - cx;
            float vy = ey - cy;
            float d = (float) Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2)); //distance between two points
            if(d > range) {
                continue;
            }

            float len = (float) Math.sqrt(d);
            float nx = len == 0 ? 0 : vx / len; //vector normalisation
            float ny = len == 0 ? 0 : vy / len;
            float dot = nx * lookX + ny * lookY;

            if(len == 0f || dot >= cosHalfAngle) {
                enemy.takeDamage(1);
                hit = true;
            }
        }

        if(hit) {
            setTint(Color.YELLOW);
        } else {
            setTint(Color.ORANGE);
        }

        // Attack cooldown duration in seconds.
        attackTimer = 0.25f;
    }

    public void speedEffect(float mult, float t, Color tint) {
        speed = mult;
        effectTimer = t;
        setTint(tint);
    }

    public void setTint(Color c) {
        this.tint = c;
        this.tintTimer = 0f;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void collectKey() {
        keys++;
    }

    public int getKeys() {
        return keys;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setColor(tint);
        renderMovingObj(batch, current);
        batch.setColor(Color.WHITE);
    }

    @Override
    public Rectangle getBounds() {
        return getBoundsMovingObj();
    }
}