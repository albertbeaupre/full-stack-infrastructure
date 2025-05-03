package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.css.*;

public class VerticalLayout extends Div {

    @Override
    protected void create() {
        this.getStyle()
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN);
    }

    @Override
    protected void destroy() {

    }
}
