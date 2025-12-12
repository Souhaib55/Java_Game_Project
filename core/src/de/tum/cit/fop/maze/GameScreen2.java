package de.tum.cit.fop.maze;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.objects.*;
import de.tum.cit.fop.maze.objects.enemies.*;
import de.tum.cit.fop.maze.objects.powerups.*;
import de.tum.cit.fop.maze.objects.traps.*;

import java.io.IOException;
import java.util.Iterator;

//TODO: rewrite the hud and pause/gameover/victory menu so that it uses scene2d
public class GameScreen2 implements Screen {
    private final MazeRunnerGame game;
    // two cameras and viewports because hud stays fixed and player moves
    private final OrthographicCamera camera, hudCam;
    private final Viewport viewport, hudVp;
    private final GameMap map;
    private final Player player;
    private boolean paused = false, gameOver = false, victory = false;
    private int pi = 0;

    private float score = 0, time = 120; // 120 seconds to finish

    private final KeyBindings binds = KeyBindings.load();

    private final ShapeRenderer sr = new ShapeRenderer();

    public GameScreen2(MazeRunnerGame game, String path) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1024, 768, camera);
        camera.setToOrtho(false, 1024, 768);

        hudCam = new OrthographicCamera();
        hudVp = new ScreenViewport(hudCam);

        map = new GameMap();
        map.load(path);

        player = new Player(map.getEx() * GameObj.TILE, map.getEy() * GameObj.TILE);
    }

    public GameScreen2(MazeRunnerGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1024, 768, camera);
        camera.setToOrtho(false, 1024, 768);

        hudCam = new OrthographicCamera();
        hudVp = new ScreenViewport(hudCam);

        map = new GameMap();
        try {
            map.generateMap();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        player = new Player(map.getEx() * GameObj.TILE, map.getEy() * GameObj.TILE);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        globalInput();

        if(!paused && !gameOver && !victory) {
            time -= 0.016f;

            player.update(delta);
            playerInput(delta);

            // update enemies
            for(Enemy e : map.getEnemies()) {
                e.update(delta, player, map);
                if(!player.isAlive()) gameOver = true;
            }

            // update traps
            for(Trap t : map.getTraps()) {
                t.update(player, delta);
                if(!player.isAlive()) gameOver = true;
            }

            // check keys
            for(Iterator<Key> it = map.getKeys().iterator(); it.hasNext(); ) {
                Key key = it.next();
                if(player.getBounds().overlaps(key.getBounds())) {
                    player.collectKey();
                    score += 100;
                    it.remove();
                }
            }

            // check powerups
            for(Iterator<Powerup> it = map.getPowerups().iterator(); it.hasNext(); ) {
                Powerup p = it.next();
                if(player.getBounds().overlaps(p.getBounds())) {
                    p.update(player, delta);
                    score += 50;
                    it.remove();
                }
            }

            // check exit
            Exit e = map.getExit();
            if(e != null) {
                if(player.getBounds().overlaps(e.getBounds()) && player.getKeys() > 0) {
                    victory = true;
                }
            }

            centerCameraOnPlayer();
        }

        if(time <= 0) {
            gameOver = true;
        }

        // draw map and entities
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();
        map.render(game.getSpriteBatch());
        player.render(game.getSpriteBatch());
        for(Enemy enemy : map.getEnemies()) {
            enemy.render(game.getSpriteBatch());
        }
        game.getSpriteBatch().end();

        // draw HUD
        game.getSpriteBatch().setProjectionMatrix(hudCam.combined);
        game.getSpriteBatch().begin();
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Lives: " + player.getHp(), 20, hudVp.getWorldHeight() - 20);
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Key: " + (player.getKeys() > 0 ? " COLLECTED" : " MISSING"), 20, hudVp.getWorldHeight() - 50);
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Time: " + (int) time, 20, hudVp.getWorldHeight() - 80);
        game.getSkin().getFont("font").draw(game.getSpriteBatch(), "Score: " + (int) score, 20, hudVp.getWorldHeight() - 110);
        Exit exit = map.getExit();
        if(exit != null) {
            game.getSkin().getFont("font").draw(game.getSpriteBatch(),
                    "Exit " + computeArrow(player.getX(), player.getY(), exit.getX() * GameObj.TILE, exit.getY() * GameObj.TILE),
                    20, hudVp.getWorldHeight() - 140);
        }
        game.getSpriteBatch().end();

        // pause menu
        if(paused && !gameOver && !victory) {
            drawDim();
            String[] pause = {"Continue", "New Map", "New Endless", "Main Menu"};
            if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) pi = (pi - 1 + pause.length) % pause.length;
            if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) pi = (pi + 1) % pause.length;
            if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                switch(pi) {
                    case 0 -> paused = false;
                    case 1 -> game.setScreen(new GameScreen2(game, "assets/map/map1.properties"));
                    case 2 -> game.setScreen(new GameScreen2(game));
                    case 3 -> game.setScreen(new MenuScreen(game));
                }
            }

            game.getSpriteBatch().begin();
            GlyphLayout tl = new GlyphLayout(game.getSkin().getFont("font"), "PAUSED");
            float tx = hudVp.getWorldWidth() / 2 - tl.width / 2;
            float ty = hudVp.getWorldHeight() - 200;
            float cx = hudVp.getWorldWidth() / 2f;
            game.getSkin().getFont("font").draw(game.getSpriteBatch(), tl, tx, ty);

            for(int idx = 0; idx < pause.length; idx++) {
                if(idx == pi) {
                    game.getSkin().getFont("font").setColor(Color.YELLOW); // yellow for selection
                } else {
                    game.getSkin().getFont("font").setColor(Color.WHITE); // white otherwise
                }
                GlyphLayout l = new GlyphLayout(game.getSkin().getFont("font"), pause[idx]);
                game.getSkin().getFont("font").draw(game.getSpriteBatch(), l, cx - l.width / 2, ty - 150 - idx * 35);
            }
            game.getSkin().getFont("font").setColor(Color.WHITE);
            game.getSpriteBatch().end();
        } else if(gameOver) {
            drawDim();
            // game over screen
            game.getSpriteBatch().begin();
            GlyphLayout tl = new GlyphLayout(game.getSkin().getFont("font"), "YOU DIED!");
            float tx = hudVp.getWorldWidth() / 2 - tl.width / 2;
            float ty = hudVp.getWorldHeight() - 200;
            game.getSkin().getFont("font").draw(game.getSpriteBatch(), tl, tx, ty);

            float cx = hudVp.getWorldWidth() / 2f;
            String[] v = new String[]{"Final score: " + (int) (score + time), "Press M to return to menu",
                    map.isEndless() ? "Press N to generate new map" : ""};
            for(int i = 0; i < v.length; i++) {
                GlyphLayout l = new GlyphLayout(game.getSkin().getFont("font"), v[i]);
                game.getSkin().getFont("font").draw(game.getSpriteBatch(), l, cx - l.width / 2, ty - 150 - i * 35);
            }
            game.getSpriteBatch().end();
        } else if(victory) {
            drawDim();
            // victory screen
            game.getSpriteBatch().begin();
            GlyphLayout tl = new GlyphLayout(game.getSkin().getFont("font"), "YOU WON!");
            float tx = hudVp.getWorldWidth() / 2 - tl.width / 2;
            float ty = hudVp.getWorldHeight() - 200;
            game.getSkin().getFont("font").draw(game.getSpriteBatch(), tl, tx, ty);

            float cx = hudVp.getWorldWidth() / 2f;
            String[] v = new String[]{"Final score: " + (int) (score + time), "Press M to return to menu",
                    map.isEndless() ? "Press N to generate new map" : ""};
            for(int i = 0; i < v.length; i++) {
                GlyphLayout l = new GlyphLayout(game.getSkin().getFont("font"), v[i]);
                game.getSkin().getFont("font").draw(game.getSpriteBatch(), l, cx - l.width / 2, ty - 150 - i * 35);
            }

            game.getSkin().getFont("font").setColor(Color.WHITE);
            game.getSpriteBatch().end();
        }
    }

    private void globalInput() {
        // toggle pause
        if(!gameOver && !victory && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }

        // return to menu
        if((gameOver || victory) && Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
        }
        if((gameOver || victory) && map.isEndless() && Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            game.setScreen(new GameScreen(game));
        }

        // camera zoom
        if(!paused) {
            if(Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS))
                camera.zoom = Math.max(0.5f, camera.zoom - 0.02f);
            if(Gdx.input.isKeyPressed(Input.Keys.MINUS)) camera.zoom = Math.min(2.0f, camera.zoom + 0.02f);
        }
    }

    private void centerCameraOnPlayer() {
        camera.position.set(player.getX() + GameObj.TILE / 2f, player.getY() + GameObj.TILE / 2f, 0);
        camera.update();
    }

    private void playerInput(float delta) {
        float baseSpeed = Gdx.input.isKeyPressed(binds.SPRINT) ? 168f : 100f;
        float speed = baseSpeed * player.getSpeed();
        float dx = 0, dy = 0;
        if(Gdx.input.isKeyPressed(binds.LEFT)) dx -= speed * delta;
        if(Gdx.input.isKeyPressed(binds.RIGHT)) dx += speed * delta;
        if(Gdx.input.isKeyPressed(binds.UP)) dy += speed * delta;
        if(Gdx.input.isKeyPressed(binds.DOWN)) dy -= speed * delta;

        // attack
        if(Gdx.input.isKeyJustPressed(binds.ATTACK)) {
            player.attack(map.getEnemies());
            for(Enemy e : map.getEnemies().stream().filter(e -> !e.isAlive()).toList()) {
                if(!e.isAlive()) {
                    score += 100;
                    map.getEnemies().remove(e);
                }
            }
        }

        if(dx == 0 && dy == 0) return;

        // collision
        Rectangle next = new Rectangle(player.getX() + dx, player.getY() + dy, GameObj.TILE, GameObj.TILE);
        if(!map.collidesWithWall(next) && !enemyOverlap(next)) player.move(dx, dy);
    }

    private boolean enemyOverlap(Rectangle rect) {
        for(Enemy e : map.getEnemies()) {
            if(!e.isAlive()) continue;
            if(rect.overlaps(e.getBounds())) return true;
        }
        return false;
    }

    private String computeArrow(float px, float py, float ex, float ey) {
        float dx = px - ex;
        float dy = py - ey;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if(d <= 8) return "HERE";
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if(angle < 0) angle += 360;
        if(angle >= 337.5 || angle < 22.5) return "<-";
        if(angle < 67.5) return "/v";
        if(angle < 112.5) return "v";
        if(angle < 157.5) return "\\v";
        if(angle < 202.5) return "->";
        if(angle < 247.5) return "/^";
        if(angle < 292.5) return "^";
        return "\\^";
    }

    private void drawDim() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.5f);
        sr.rect(camera.position.x - camera.viewportWidth * camera.zoom / 2, camera.position.y - camera.viewportHeight * camera.zoom / 2, camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom);
        sr.end();
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        viewport.update(width, height);
        hudVp.update(width, height, true);
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
