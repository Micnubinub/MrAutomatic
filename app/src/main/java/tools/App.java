package tools;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by root on 9/08/14.
 */

public class App implements Serializable {
    private String address;
    private String name;
    private Drawable icon;

    public App(String name, String address, Drawable icon) {
        this.address = address;
        this.name = name;
        this.icon = icon;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }
}
