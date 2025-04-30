package infrastructure.web.ui.components;

import infrastructure.web.ui.Component;
import infrastructure.web.ui.css.*;

public class VerticalLayout extends Component {
    @Override
    protected void create() {
        this.getStyle().textAlign(TextAlign.CENTER)
                .borderRadius("5px")
                .border("1px solid")
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .alignContent(AlignContent.CENTER)
                .justifyContent(JustifyContent.CENTER);


    }

    @Override
    protected void destroy() {

    }

    @Override
    public String tag() {
        return "div";
    }
}
