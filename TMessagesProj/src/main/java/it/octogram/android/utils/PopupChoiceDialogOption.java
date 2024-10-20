package it.octogram.android.utils;

public class PopupChoiceDialogOption {
    public int id;
    public String itemTitle;
    String itemDescription = null;
    int itemIcon = 0;
    boolean clickable = true;

    public PopupChoiceDialogOption setId(int id) {
        this.id = id;
        return this;
    }

    public PopupChoiceDialogOption setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
        return this;
    }

    public PopupChoiceDialogOption setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
        return this;
    }

    public PopupChoiceDialogOption setItemIcon(int itemIcon) {
        this.itemIcon = itemIcon;
        return this;
    }

    public PopupChoiceDialogOption setClickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }
}
