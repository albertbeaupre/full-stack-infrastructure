package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.DOMUpdateParam;
import infrastructure.net.web.ui.DOMUpdateType;

public class Label extends Component {

    private String text;

    public Label(String text) {
        super("label");

        setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

        this.queueForDispatch(DOMUpdateType.SET_TEXT, DOMUpdateParam.TEXT, text);
        push();
    }

    @Override
    protected void create() {

    }

    @Override
    protected void destroy() {

    }
}
