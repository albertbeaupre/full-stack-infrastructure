package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.DOMUpdateParam;
import infrastructure.net.web.ui.DOMUpdateType;

public class PasswordField extends TextField {

    public PasswordField() {
        super("input");
    }

    public PasswordField(String placeholder) {
        super(placeholder);
    }

    @Override
    protected void create() {
        super.create();

        this.queueForDispatch(DOMUpdateType.SET_TYPE, DOMUpdateParam.TYPE, "password");
    }
}
