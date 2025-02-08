package infrastructure.gdx.ui;

public interface UIComponent<Style extends UIComponentStyle> {

    void render(Style style);

}
