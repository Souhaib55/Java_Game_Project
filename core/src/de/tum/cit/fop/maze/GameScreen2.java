package de.tum.cit.fop.maze;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.*;
import de.tum.cit.fop.maze.objects.*;
import de.tum.cit.fop.maze.objects.enemies.*;
import de.tum.cit.fop.maze.objects.powerups.*;
import de.tum.cit.fop.maze.objects.traps.*;

import java.io.IOException;
import java.util.Iterator;

public class GameScreen2 implements Screen {
    private final MazeRunnerGame game;
    // two cameras and viewports because hud stays fixed and player moves
    private final OrthographicCamera camera, hudCam;
    private final Viewport viewport, hudVp;
    private final GameMap map;
    private final Player player;
    private boolean paused = false, gameOver = false, victory = false;

    private float score = 0, time = 120; // 120 seconds to finish
    private int currentLevel = 0; // 0 for endless, 1-5 for levels

    private final KeyBindings binds = KeyBindings.load();

    private final ShapeRenderer sr = new ShapeRenderer();

    private Stage pauseStage, gameOverStage, victoryStage;
    private Table pauseTable, gameOverTable, victoryTable;
    private Image pauseBg, gameOverBg, victoryBg;
    private Label gameOverScore, victoryScore;

    public GameScreen2(MazeRunnerGame game, String path) {
        this.game = game;

        // Extract level number from path (e.g., "maps/level-3.properties" -> 3)
        if (path.contains("level-")) {
            try {
                String levelStr = path.substring(path.indexOf("level-") + 6, path.indexOf(".properties"));
                currentLevel = Integer.parseInt(levelStr);
            } catch (Exception e) {
                currentLevel = 1;
            }
        } else {
            currentLevel = 0; // endless mode
        }

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1024, 768, camera);
        camera.setToOrtho(false, 1024, 768);

        hudCam = new OrthographicCamera();
        hudVp = new ScreenViewport(hudCam);

        map = new GameMap();
        map.load(path);

        player = new Player(map.getEx() * GameObj.TILE, map.getEy() * GameObj.TILE);

        initPauseMenu();
        initGameOverMenu();
        initVictoryMenu();
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

        initPauseMenu();
        initGameOverMenu();
        initVictoryMenu();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        globalInput();

        if(!paused && !gameOver && !victory) {
            time -= delta;

            player.update(delta);
            playerInput(delta);

            // update enemies
            for(Enemy e : map.getEnemies()) {
                e.update(delta, player, map);
                if(!player.isAlive()) {
                    gameOver = true;
                    updateGameOverScore();
                }
            }

            // update traps
            for(Trap t : map.getTraps()) {
                t.update(player, delta);
                if(!player.isAlive()) {
                    gameOver = true;
                    updateGameOverScore();
                }
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
                    updateVictoryScore();
                }
            }

            centerCameraOnPlayer();
        }

        if(time <= 0) {
            gameOver = true;
            updateGameOverScore();
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

        //update & render stages
        if(paused && !gameOver && !victory) {
            pauseBg.setVisible(true);
            pauseTable.setVisible(true);
            pauseStage.act(delta);
            pauseStage.draw();
        } else if(gameOver) {
            gameOverBg.setVisible(true);
            gameOverTable.setVisible(true);
            gameOverStage.act(delta);
            gameOverStage.draw();
        } else if(victory) {
            victoryBg.setVisible(true);
            victoryTable.setVisible(true);
            victoryStage.act(delta);
            victoryStage.draw();
        } else {
            // Hide all backgrounds when no menu is active
            pauseBg.setVisible(false);
            pauseTable.setVisible(false);
            gameOverBg.setVisible(false);
            gameOverTable.setVisible(false);
            victoryBg.setVisible(false);
            victoryTable.setVisible(false);
        }
    }

    //TODO: make code below less repetitive (make helper methods)
    private void initPauseMenu() {
        pauseStage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        pauseBg = new Image(new Texture(Gdx.files.internal("white.png")));
        pauseBg.setColor(0, 0, 0, 0.5f);
        pauseBg.setFillParent(true);
        pauseBg.setVisible(false);

        pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.setVisible(false);

        Label title = new Label("PAUSED", game.getSkin(), "title");
        pauseTable.add(title).padBottom(80).row();

        String[] menuItems = {"Continue", "New Map", "New Endless", "Main Menu"};
        for(String item : menuItems) {
            TextButton button = new TextButton(item, game.getSkin());
            pauseTable.add(button).width(320).padBottom(20).row();

            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    handlePauseMenuSelection(item);
                }
            });
        }

        pauseStage.addActor(pauseBg);
        pauseStage.addActor(pauseTable);
    }

    private void initGameOverMenu() {
        gameOverStage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        gameOverBg = new Image(new Texture(Gdx.files.internal("white.png")));
        gameOverBg.setColor(0, 0, 0, 0.5f);
        gameOverBg.setFillParent(true);
        gameOverBg.setVisible(false);

        gameOverTable = new Table();
        gameOverTable.setFillParent(true);
        gameOverTable.setVisible(false);

        Label title = new Label("YOU DIED!", game.getSkin(), "title");
        gameOverTable.add(title).padBottom(80).row();

        gameOverScore = new Label("", game.getSkin());
        gameOverTable.add(gameOverScore).padBottom(20).row();

        TextButton menuButton = new TextButton("Main Menu", game.getSkin());
        gameOverTable.add(menuButton).width(320).padBottom(20).row();

        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MenuScreen(game));
            }
        });

        if(map.isEndless()) {
            TextButton newMapButton = new TextButton("Generate New Map", game.getSkin());
            gameOverTable.add(newMapButton).width(320).padBottom(20).row();

            newMapButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new GameScreen2(game));
                }
            });
        }

        gameOverStage.addActor(gameOverBg);
        gameOverStage.addActor(gameOverTable);
    }

    private void initVictoryMenu() {
        victoryStage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        victoryBg = new Image(new Texture(Gdx.files.internal("white.png")));
        victoryBg.setColor(0, 0, 0, 0.5f);
        victoryBg.setFillParent(true);
        victoryBg.setVisible(false);

        victoryTable = new Table();
        victoryTable.setFillParent(true);
        victoryTable.setVisible(false);

        Label title = new Label("YOU WON!", game.getSkin(), "title");
        victoryTable.add(title).padBottom(80).row();

        victoryScore = new Label("", game.getSkin());
        victoryTable.add(victoryScore).padBottom(20).row();

        // Add Next Level button for levels 1-4
        if(currentLevel > 0 && currentLevel < 5) {
            TextButton nextLevelButton = new TextButton("Next Level", game.getSkin());
            victoryTable.add(nextLevelButton).width(320).padBottom(20).row();

            nextLevelButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.goToLevel(currentLevel + 1);
                }
            });
        }

        TextButton menuButton = new TextButton("Main Menu", game.getSkin());
        victoryTable.add(menuButton).width(320).padBottom(20).row();

        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MenuScreen(game));
            }
        });

        if(map.isEndless()) {
            TextButton newMapButton = new TextButton("Generate New Map", game.getSkin());
            victoryTable.add(newMapButton).width(320).padBottom(20).row();

            newMapButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new GameScreen2(game));
                }
            });
        }

        victoryStage.addActor(victoryBg);
        victoryStage.addActor(victoryTable);
    }

    private void handlePauseMenuSelection(String item) {
        switch(item) {
            case "Continue":
                paused = false;
                Gdx.input.setInputProcessor(null);
                break;
            case "New Map":
                game.setScreen(new GameScreen2(game, "assets/map/map1.properties"));
                break;
            case "New Endless":
                game.setScreen(new GameScreen2(game));
                break;
            case "Main Menu":
                game.setScreen(new MenuScreen(game));
                break;
        }
    }

    private void updateGameOverScore() {
        gameOverScore.setText("Final score: " + (int) (score + time));
    }

    private void updateVictoryScore() {
        victoryScore.setText("Final score: " + (int) (score + time));
    }

    private void globalInput() {
        // toggle pause
        if(!gameOver && !victory && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            if(paused) {
                Gdx.input.setInputProcessor(pauseStage);
            } else {
                Gdx.input.setInputProcessor(null);
            }
        }

        //set input processor for game over/victory screens
        if(gameOver && Gdx.input.getInputProcessor() != gameOverStage) {
            Gdx.input.setInputProcessor(gameOverStage);
        } else if(victory && Gdx.input.getInputProcessor() != victoryStage) {
            Gdx.input.setInputProcessor(victoryStage);
        }

        // camera zoom (only when not in any menu)
        if(!paused && !gameOver && !victory) {
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

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        viewport.update(width, height);
        hudVp.update(width, height, true);
        pauseStage.getViewport().update(width, height, true);
        gameOverStage.getViewport().update(width, height, true);
        victoryStage.getViewport().update(width, height, true);
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
        sr.dispose();
        pauseStage.dispose();
        gameOverStage.dispose();
        victoryStage.dispose();
    }
}
