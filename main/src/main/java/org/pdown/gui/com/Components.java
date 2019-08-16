package org.pdown.gui.com;

import org.pdown.gui.util.I18nUtil;

import javax.swing.*;
import java.io.File;

public class Components {

  /**
   * 弹出提示窗，窗口置顶
   */
  public static void alert(String msg) {
    JOptionPane.showMessageDialog(null, msg, I18nUtil.getMessage("gui.warning"), JOptionPane.WARNING_MESSAGE);
  }

  /**
   * 弹出文件选择框
   */
  public static File fileChooser() {
//    Stage stage = buildBackgroundTopStage();
//    FileChooser chooser = new FileChooser();
//    chooser.setTitle("选择文件");
//    File file = chooser.showOpenDialog(stage);
//    stage.close();
//    return file;
    return null;
  }

  /**
   * 弹出文件夹选择框
   */
  public static File dirChooser() {
//    Stage stage = buildBackgroundTopStage();
//    DirectoryChooser chooser = new DirectoryChooser();
//    chooser.setTitle("选择文件夹");
//    File file = chooser.showDialog(stage);
//    stage.close();
//    return file;
    return null;
  }

//  private static Stage buildBackgroundTopStage() {
//    Stage stage = new Stage();
//    stage.setAlwaysOnTop(true);
//    stage.setWidth(1);
//    stage.setHeight(1);
//    stage.initStyle(StageStyle.UNDECORATED);
//    stage.show();
//    return stage;
//  }
}
