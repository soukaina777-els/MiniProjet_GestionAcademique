package exrc_miniprojet.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CoursItem {

    private final Cours cours;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public CoursItem(Cours cours) {
        this.cours = cours;
    }

    public Cours getCours() {
        return cours;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    @Override
    public String toString() {
        // texte affiché à côté de la checkbox
        return cours.getCode() + " - " + cours.getIntitule();
    }
}
