package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.DOMUpdateParam;
import infrastructure.net.web.ui.DOMUpdateType;
import infrastructure.net.web.ui.ValueComponent;

import java.util.Map;

public class Checkbox extends ValueComponent<Boolean> {

    private boolean checked;

    public Checkbox() {
        super("input");

    }

    public void setChecked(boolean checked) {
        this.checked = checked;

        this.queueForDispatch(DOMUpdateType.SET_PROPERTY, Map.of(DOMUpdateParam.PROPERTY, "checked", DOMUpdateParam.VALUE, String.valueOf(checked)));
        this.push();

        System.out.println("checked: " + checked);
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public Boolean deconstruct(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public String construct(Boolean value) {
        return String.valueOf(value);
    }

    @Override
    protected void create() {
        this.queueForDispatch(DOMUpdateType.SET_TYPE, Map.of(DOMUpdateParam.TYPE, "checkbox"));
    }

    @Override
    protected void destroy() {

    }
}
