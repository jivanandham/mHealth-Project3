package edu.pitt.lersais.mhealth.model;

/**
 * Created by runhua on 6/20/18.
 */

public class ExpandedMenuItem {
    int menuNameId;
    int menuIconId;

    public ExpandedMenuItem(int nameId, int iconId) {
        this.menuNameId = nameId;
        this.menuIconId = iconId;
    }

    public int getMenuName() {return menuNameId;}

    public int getMenuIconId() {return menuIconId;}

    public void setMenuName(int _menuNameId) {this.menuNameId = _menuNameId;}

    public void setMenuIconId(int _menuIconId) {this.menuIconId = _menuIconId;}

}
