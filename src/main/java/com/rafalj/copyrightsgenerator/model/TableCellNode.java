package com.rafalj.copyrightsgenerator.model;

import javafx.scene.Node;
import javafx.scene.control.TableCell;

public abstract class TableCellNode<S, T> extends TableCell<S, T> {

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
            return;
        }

        setGraphic(decorate(item));
    }

    protected abstract Node decorate(T item);
}
