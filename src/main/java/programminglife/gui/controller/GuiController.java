package programminglife.gui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import programminglife.ProgrammingLife;
import programminglife.model.Graph;
import programminglife.model.exception.UnknownTypeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

/**
 * The controller for the GUI that is used in the application.
 * The @FXML tag is needed in initialize so that javaFX knows what to do.
 */
public class GuiController {

    @FXML private MenuItem btnOpen;
    @FXML private MenuItem btnQuit;
    @FXML private Canvas graphCanvas;
    @FXML private TextField maxDepthText;
    @FXML private Button drawButton;
    @FXML private Button translate;

    private int translateX;
    private int translateY;
    private GraphController graphController;

    @FXML @SuppressWarnings("Unused")
    /**
     * The initialize will call the other methods that are run in the GUI
     */
    private void initialize() {
        this.graphController = new GraphController(null, this.graphCanvas);
        initApp();
        initButtons();
    }

    /**
     * Open and parse a file.
     * @param file The {@link File} to open
     * @throws FileNotFoundException if the {@link File} is not found
     * @throws UnknownTypeException if the {@link File} is not compliant with the GFA standard
     */
    public void openFile(File file) throws FileNotFoundException, UnknownTypeException {
        if (file != null) {
            Graph graph = Graph.parse(file, true);
            this.graphController.setGraph(graph);
        } else {
            throw new Error("WTF this file is null");
        }
    }

    /**
     * Initializes the open button so that the user can decide which file to open.
     * Sets the action for the open MenuItem.
     * Sets the event for the quit MenuItem.
     */
    private void initApp() {
        btnOpen.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            final ExtensionFilter extFilterGFA = new ExtensionFilter("GFA files (*.gfa)", "*.GFA");
            fileChooser.getExtensionFilters().add(extFilterGFA);

            try {
                File file = fileChooser.showOpenDialog(ProgrammingLife.getStage());
                this.openFile(file);
            } catch (FileNotFoundException | UnknownTypeException e) {
                // Should not happen, because it gets handled by FileChooser and ExtensionFilter
                throw new RuntimeException("This should absolutely not have happened", e);
            }
        });

        btnQuit.setOnAction(event -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Confirm Exit");
            a.setHeaderText("Do you really want to exit?");
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK) {
                Platform.exit();
                System.exit(0);
            }

            if (result.get() == ButtonType.CANCEL) {
                a.close();
            }
        });

        drawButton.setOnAction(event -> {
            int maxDepth = Integer.MAX_VALUE;
            System.out.printf("TextField text: %s\n", maxDepthText.getText());

            try {
                maxDepth = Integer.parseInt(maxDepthText.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.printf("Draw graph with max depth %d\n", maxDepth);
            this.graphController.clear();
            this.graphController.draw(maxDepth);
        });
    }

    private void initButtons() {
        translate.setOnAction(event -> {
            GridPane root = new GridPane();
            TextField f1 = new TextField();
            root.add(new Label("X value"), 0, 0);
            root.add(f1, 1, 0);
            TextField f2 = new TextField();
            root.add(new Label("Y value"), 0, 1);
            root.add(f2, 1, 1);
            Button ok = new Button("Translate");
            root.add(ok, 1, 2);
            Stage s = new Stage();
            s.setScene(new Scene(root, 300, 200));
            s.show();
            ok.setOnAction(event2 -> {
                this.translateX = Integer.valueOf(f1.getText());
                this.translateY = Integer.valueOf(f2.getText());
                graphCanvas.setTranslateX(this.translateX);
                graphCanvas.setTranslateY(this.translateY);
                s.close();
            });
        });
    }
}