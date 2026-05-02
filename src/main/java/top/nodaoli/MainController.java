package top.nodaoli;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

public class MainController {

    @FXML
    private TextField sourcePathField;

    @FXML
    private TextField targetPathField;

    @FXML
    public void initialize() {
        setupDragAndDrop(sourcePathField);
        setupDragAndDrop(targetPathField);
    }

    private void setupDragAndDrop(TextField textField) {
        textField.setOnDragOver((DragEvent event) -> {
            if (event.getGestureSource() != textField && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        textField.setOnDragDropped((DragEvent event) -> {
            boolean success = false;
            if (event.getDragboard().hasFiles()) {
                File file = event.getDragboard().getFiles().get(0);
                String droppedPath = file.getAbsolutePath();
                
                if (textField == targetPathField && file.isDirectory()) {
                    String sourceStr = sourcePathField.getText();
                    if (sourceStr != null && !sourceStr.trim().isEmpty()) {
                        File sourceFile = new File(sourceStr);
                        droppedPath = new File(file, sourceFile.getName()).getAbsolutePath();
                    } else {
                        if (!droppedPath.endsWith(File.separator)) {
                            droppedPath += File.separator;
                        }
                    }
                }
                
                textField.setText(droppedPath);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    void onBrowseSourceFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择源文件");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            sourcePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void onBrowseSourceDir(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("选择源目录");
        File dir = dirChooser.showDialog(null);
        if (dir != null) {
            sourcePathField.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    void onBrowseTarget(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("选择软链接保存目录");
        File dir = dirChooser.showDialog(null);
        if (dir != null) {
            String targetPath = dir.getAbsolutePath();
            String sourceStr = sourcePathField.getText();
            if (sourceStr != null && !sourceStr.trim().isEmpty()) {
                File sourceFile = new File(sourceStr);
                targetPath = new File(dir, sourceFile.getName()).getAbsolutePath();
            } else {
                if (!targetPath.endsWith(File.separator)) {
                    targetPath += File.separator;
                }
            }
            targetPathField.setText(targetPath);
        }
    }

    @FXML
    void onConfirm(ActionEvent event) {
        String sourceStr = sourcePathField.getText();
        String targetStr = targetPathField.getText();

        if (sourceStr == null || sourceStr.trim().isEmpty() || targetStr == null || targetStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "路径不能为空！");
            return;
        }

        Path sourcePath = Paths.get(sourceStr);
        Path targetPath = Paths.get(targetStr);

        if (!Files.exists(sourcePath)) {
            showAlert(Alert.AlertType.ERROR, "错误", "源文件或目录不存在！");
            return;
        }

        if (Files.isDirectory(targetPath)) {
            Path fileName = sourcePath.getFileName();
            if (fileName != null) {
                targetPath = targetPath.resolve(fileName);
                targetPathField.setText(targetPath.toString());
            }
        }

        if (Files.exists(targetPath)) {
            showAlert(Alert.AlertType.ERROR, "错误", "软链接目标路径已存在，请指定一个不存在的文件名！");
            return;
        }

        try {
            boolean isDir = Files.isDirectory(sourcePath);
            String command;
            if (isDir) {
                command = String.format("cmd /c mklink /d \"%s\" \"%s\"", targetPath.toString(), sourcePath.toString());
            } else {
                command = String.format("cmd /c mklink \"%s\" \"%s\"", targetPath.toString(), sourcePath.toString());
            }

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                showAlert(Alert.AlertType.INFORMATION, "成功", "软链接创建成功！\n" + targetPath.toString());
            } else {
                showAlert(Alert.AlertType.ERROR, "失败", "软链接创建失败，可能需要管理员权限，或开启开发者模式。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "异常", "发生错误：\n" + e.getMessage());
        }
    }

    @FXML
    void onCancel(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
