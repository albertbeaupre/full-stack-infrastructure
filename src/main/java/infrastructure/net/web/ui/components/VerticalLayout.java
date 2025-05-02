package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.css.*;

public class VerticalLayout extends Div {

    @Override
    protected void create() {
        this.getStyle().textAlign(TextAlign.CENTER)
                .borderRadius("5px")
                .border("1px solid")
                .padding("10px")
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .alignContent(AlignContent.CENTER)
                .justifyContent(JustifyContent.CENTER);
    }

    @Override
    protected void destroy() {

    }
}
