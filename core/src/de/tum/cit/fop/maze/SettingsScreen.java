package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final KeyBindings binds = KeyBindings.load();

    private final String[] items = {
            "Move Up",
            "Move Down",
            "Move Left",
            "Move Right",
            "Attack",
            "Sprint",
            "Back"
    };

    private final TextButton[] buttons;
    private final Label statusLabel;

    private int i = 0;
    private boolean waiting = false;

    public SettingsScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();
        stage = new Stage(new ScreenViewport(camera), game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Label title = new Label("SETTINGS", game.getSkin(), "title");
        // table.add(title).padBottom(-80).row();

        buttons = new TextButton[items.length];
        for(int idx = 0; idx < items.length; idx++) {
            String text = getButtonText(idx);
            TextButton button = new TextButton(text, game.getSkin());
            buttons[idx] = button;
            table.add(button).width(320).padBottom(20).row();
            final int index = idx;
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if(index == items.length - 1) {
                        game.setScreen(new MenuScreen(game));
                    } else {
                        waiting = true;
                        i = index;
                        updateStatus();
                    }
                }
            });
        }

        statusLabel = new Label("", game.getSkin());
        table.add(statusLabel).padTop(40).row();
        updateStatus();
    }

    private String getButtonText(int idx) {
        if(idx == items.length - 1) return items[idx];
        return items[idx] + ": " + Input.Keys.toString(getCurrentBind(idx));
    }

    private void updateButtons() {
        for(int idx = 0; idx < items.length; idx++) {
            buttons[idx].setText(getButtonText(idx));
        }
    }

    private void updateStatus() {
        if(waiting) {
            statusLabel.setText("Press a key (ESC to cancel)");
        } else {
            statusLabel.setText("Click to change, ESC to return");
        }
    }

    private void handleInput() {
        if(!waiting) {
            if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                game.setScreen(new MenuScreen(game));
            }
        } else {
            if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                waiting = false;
                updateStatus();
                return;
            }
            for(int k = 0; k <= Input.Keys.MAX_KEYCODE; k++) {
                if(Gdx.input.isKeyJustPressed(k)) {
                    applyBind(i, k);
                    binds.save();
                    waiting = false;
                    updateButtons();
                    updateStatus();
                    break;
                }
            }
        }
    }

    private void applyBind(int i, int key) {
        switch(i) {
            case 0 -> binds.UP = key;
            case 1 -> binds.DOWN = key;
            case 2 -> binds.LEFT = key;
            case 3 -> binds.RIGHT = key;
            case 4 -> binds.ATTACK = key;
            case 5 -> binds.SPRINT = key;
        }
    }

    private int getCurrentBind(int i) {
        return switch(i) {
            case 0 -> binds.UP;
            case 1 -> binds.DOWN;
            case 2 -> binds.LEFT;
            case 3 -> binds.RIGHT;
            case 4 -> binds.ATTACK;
            case 5 -> binds.SPRINT;
            default -> -1;
        };
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        handleInput();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
