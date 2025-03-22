package infrastructure.gdx.ui;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.SnapshotArray;
import infrastructure.event.EventListener;
import infrastructure.gdx.ui.css.PropertyChangeEvent;
import infrastructure.gdx.ui.css.Style;

public class UIElement implements EventListener<PropertyChangeEvent>, InputProcessor {

    private static final UIElement TOP_LEVEL_ELEMENT = new UIElement();
    private static long UUID = 0;

    static {
        /** TODO
         TOP_LEVEL_ELEMENT.getStyle()
         .width(Gdx.graphics.getWidth() + "px")
         .height(Gdx.graphics.getHeight() + "px")
         .lock();
         *
         */
    }

    private final Style style = new Style(this, ".class-" + UUID++);
    private SnapshotArray<UIElement> children = new SnapshotArray<>();
    private UIElement parent;

    public UIElement() {

    }

    public UIElement listen() {

        return this;
    }

    public Style getStyle() {
        return style;
    }

    @Override
    public void handle(PropertyChangeEvent event) {
        System.out.println(STR."PROPERTY CHANGED BICCCH: \{event.getProperty()}=\{event.getValue()}");
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
