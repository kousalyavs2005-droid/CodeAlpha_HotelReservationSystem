package ui;

/** Panels implementing this reload their data every time they are shown. */
public interface Refreshable {
    void refresh();
}
