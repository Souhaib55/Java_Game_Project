package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import de.tum.cit.fop.maze.objects.*;
import de.tum.cit.fop.maze.objects.enemies.*;
import de.tum.cit.fop.maze.objects.powerups.*;
import de.tum.cit.fop.maze.objects.traps.*;

import java.io.*;
import java.util.*;

public class GameMap {
    private int w, h;
    private final Map<String, Integer> tiles = new HashMap<>();
    private int ex, ey;
    private final Texture wall = new Texture("assets/wall.png"),
            entry = new Texture("assets/entry.png"),
            floor = new Texture("assets/entry.png");
    private final List<Trap> traps = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Powerup> powerups = new ArrayList<>();
    private Exit exit;

    private boolean isEndless = false;

    public void load(String path) {
        Properties p = new Properties();
        try(BufferedReader r = new BufferedReader(Gdx.files.internal(path).reader())) {
            p.load(r);
        } catch(IOException e) {
            throw new RuntimeException("Failed to load map.", e);
        }

        int maxX = 0, maxY = 0;
        for(String key : p.stringPropertyNames()) {
            int val = Integer.parseInt(p.getProperty(key).trim());
            tiles.put(key, val);

            String[] split = key.split(",");
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);

            if(x > maxX) {
                maxX = x;
            }
            if(y > maxY) {
                maxY = y;
            }

            switch(val) {
                case 1 -> {
                    ex = x;
                    ey = y;
                }
                case 2 -> exit = new Exit(x, y);
                case 3 -> traps.add(Math.round(Math.random()) == 0 ? //random trap type
                        new SludgeTrap(x, y, 0.2f) : new DamageTrap(x, y, 1f));
                case 4 -> enemies.add(Math.round(Math.random()) == 0 ? //random enemy type
                        new ChaserEnemy(x, y) : new PatrolEnemy(x, y, Math.round(Math.random()) == 0));
                case 5 -> keys.add(new Key(x, y));
                case 6 -> powerups.add(Math.round(Math.random()) == 0 ? //random enemy type
                        new Hpup(x, y) : new Speedup(x, y));
            }
        }
        w = maxX + 1;
        h = maxY + 1;
    }

    public void render(SpriteBatch batch) {
        for(int y = 0; y < h; y++) {
            for(int x = 0; x < w; x++) {
                Integer v = tiles.get(x + "," + y);
                Texture t;
                if(v == null) {
                    t = floor;
                } else {
                    t = switch(v) {
                        case 0 -> wall;
                        case 1 -> entry;
                        default -> floor;
                    };
                }
                batch.draw(t, x * GameObj.TILE, y * GameObj.TILE, GameObj.TILE, GameObj.TILE);
            }
        }
        traps.forEach(trap -> trap.render(batch));
        keys.forEach(key -> key.render(batch));
        enemies.forEach(enemy -> enemy.render(batch));
        powerups.forEach(powerup -> powerup.render(batch));
        exit.render(batch);
    }

    public Integer getTile(int x, int y) {
        //treat out of bounds as wall tile
        if(x < 0 || x >= w || y < 0 || y >= h) {
            return 0;
        }
        return tiles.get(x + "," + y);
    }

    public boolean isWall(int x, int y) {
        Integer v = getTile(x, y);
        return v != null && v == 0;
    }

    public boolean collidesWithWall(Rectangle r) {
        int t = GameObj.TILE;

        // shrink hitbox slightly
        float margin = 0.1f * t;
        Rectangle hitbox = new Rectangle(
                r.x + margin,
                r.y + margin,
                r.width - 2 * margin,
                r.height - 2 * margin
        );

        int minTX = (int) Math.floor(hitbox.x / t);
        int maxTX = (int) Math.ceil((hitbox.x + hitbox.width) / t);
        int minTY = (int) Math.floor(hitbox.y / t);
        int maxTY = (int) Math.ceil((hitbox.y + hitbox.height) / t);

        //check if any part of the hitbox is outside of map boundaries
        if(minTX < 0 || maxTX >= w || minTY < 0 || maxTY >= h) {
            return true;
        }

        for(int x = minTX; x <= Math.min(w - 1, maxTX); x++) {
            for(int y = minTY; y <= Math.min(h - 1, maxTY); y++) {
                if(this.isWall(x, y)) {
                    Rectangle wallRect = new Rectangle(x * t, y * t, t, t);
                    if(hitbox.overlaps(wallRect)) return true;
                }
            }
        }
        return false;
    }

    //TODO: implement smarter generation
    public void generateMap() throws IOException {
        int n = 20, key = 0, trap = 0, enemy = 0, power = 0;
        isEndless = true;

        FileHandle file = Gdx.files.local("maps/endless.properties");
        if(file.exists()) {
            file.delete();
        }

        StringBuilder sb = new StringBuilder(10000);
        BufferedWriter bw = new BufferedWriter(file.writer(false));

        //outer walls
        for(int i = 0; i < n; i++) {
            sb.append(i).append(",0=0\n");
            sb.append("0,").append(i).append("=0\n");
            sb.append((n - 1)).append(",").append(i).append("=0\n");
            sb.append(i).append(",").append(n - 1).append("=0\n");
        }

        //entry,exit
        sb.append("1,1=1\n");
        sb.append((n - 2)).append(",").append(n - 2).append("=2\n");

        for(int i = 2; i < n - 2; i++) {
            for(int j = 2; j < n - 2; j++) {
                int what = (int) (Math.random() * 7);
                switch(what) {
                    case 3:
                        if(trap < 5) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                            trap++;
                        }
                        break;  // ← prevents fall-through
                    case 4:
                        if(enemy < 5 && (i > 3 && j > 3)) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                            enemy++;
                        }
                        break;
                    case 5:
                        if(key < 3) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                            key++;
                        }
                        break;
                    case 6:
                        if(power < 5) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                            power++;
                        }
                        break;
                    default:
                        if(Math.round(Math.random()) > 0 && !(what == 1 || what == 2)) {
                            sb.append(i).append(",").append(j).append("=").append(what).append("\n");
                        }
                        break;
                }
            }
        }
        bw.write(sb.toString());
        bw.close();

        load("maps/endless.properties");
    }

    public boolean isEndless() {
        return isEndless;
    }

    public int getEx() {
        return ex;
    }

    public int getEy() {
        return ey;
    }

    public Exit getExit() {
        return exit;
    }

    public List<Trap> getTraps() {
        return traps;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Powerup> getPowerups() {
        return powerups;
    }
}