package infrastructure.web.ui.components;

import infrastructure.web.ui.Component;
import infrastructure.web.ui.DOMUpdate;
import infrastructure.web.ui.DOMUpdateParam;
import infrastructure.web.ui.DOMUpdateType;
import infrastructure.web.ui.css.TextAlign;

public class Button extends Component {

    private String text;

    public Button(String text) {
        this.text = text;
    }

    public Button() {
        this.text = null;
    }

    public void setText(String text) {
        if (text == null)
            return;
        this.text = text;
        this.dispatch(new DOMUpdate(DOMUpdateType.SET_TEXT, this.getComponentID()).param(DOMUpdateParam.TEXT, text));
    }

    @Override
    protected void create() {
        this.getStyle().textAlign(TextAlign.CENTER);
        setText(text);
    }

    @Override
    protected void destroy() {

    }

    @Override
    public String tag() {
        return "button";
    }
}
