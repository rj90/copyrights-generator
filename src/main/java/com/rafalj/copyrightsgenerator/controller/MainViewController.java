package com.rafalj.copyrightsgenerator.controller;

import com.rafalj.copyrightsgenerator.engine.GitFinder;
import com.rafalj.copyrightsgenerator.model.GitSearchResult;
import com.rafalj.copyrightsgenerator.model.TableCellNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    protected final ObservableList<GitSearchResult> gitSearchResults = FXCollections.observableArrayList();

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    private ComboBox<String> cbxSrcFolder;

    @FXML
    private ComboBox<String> cbxDstFolder;

    @FXML
    private DatePicker dpFromDate;
    @FXML
    private DatePicker dpToDate;

    @FXML
    public TableView<GitSearchResult> tbResult;

    @FXML
    public TableColumn<GitSearchResult, String> tcMessage;

    @FXML
    public TableColumn<GitSearchResult, Date> tcDate;

    @FXML
    public ProgressBar tbProgress;

    private final GitFinder gitFinder = new GitFinder();

    @FXML
    public void search() {
        Task<Void> finished = new Task<>() {
            @Override
            protected Void call() {
                try {
                    gitSearchResults.clear();
                    gitSearchResults.addAll(gitFinder.generateReports(cbxSrcFolder.getValue(), dpFromDate.getValue(), dpToDate.getValue()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (GitAPIException e) {
                    throw new RuntimeException(e);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
        tbProgress.progressProperty().bind(finished.progressProperty());
        tbProgress.visibleProperty().bind(finished.runningProperty());
        tbResult.visibleProperty().bind(finished.runningProperty().not());
        new Thread(finished).start();
    }

    @FXML
    public void save() {
        Task<Void> finished = new Task<>() {
            @Override
            protected Void call() {
                try {
                    gitFinder.saveReport(cbxSrcFolder.getValue(), cbxDstFolder.getValue(), gitSearchResults);
                } catch (GitAPIException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
        tbProgress.progressProperty().bind(finished.progressProperty());
        tbProgress.visibleProperty().bind(finished.runningProperty());
        tbResult.visibleProperty().bind(finished.runningProperty().not());
        new Thread(finished).start();
    }

    @FXML
    public void chooseSrcFolder() {
        chooseFolder(cbxSrcFolder);
    }

    @FXML
    public void chooseDstFolder() {
        chooseFolder(cbxDstFolder);
    }

    private void chooseFolder(ComboBox<String> cbxDstFolder) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            String folderPath = selectedDirectory.getAbsolutePath();
            if (!cbxDstFolder.getItems().contains(folderPath)) {
                cbxDstFolder.getItems().add(folderPath);
            }
            cbxDstFolder.getSelectionModel().select(folderPath);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tbResult.setItems(gitSearchResults);

        tcMessage.setCellFactory(column -> new TableCellNode<>() {
            @Override
            protected Node decorate(String item) {
                return createTcMessage(item);
            }
        });

        tcDate.setCellFactory(column -> new TableCellNode<>() {
            @Override
            protected Node decorate(Date item) {
                return createTcDate(item);
            }
        });
    }

    private HBox createTcMessage(String item) {
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(new Label(item));
        return hBox;
    }

    private HBox createTcDate(Date item) {
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().add(new Label(sdf.format(item)));
        return hBox;
    }
}