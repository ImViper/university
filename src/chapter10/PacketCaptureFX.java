package chapter10;
/**
 * Author:蔡诚杰
 * Data:2020/11/15
 * description:
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jpcap.JpcapCaptor;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;

public class PacketCaptureFX extends Application {
    private TextArea taDisplay = new TextArea();

    private Button btnStart = new Button("开始抓包");
    private Button btnStop = new Button("停止抓包");
    private Button btnClear = new Button("清空");
    private Button btnSetting = new Button("设置");
    private Button btnExit = new Button("退出");

    private ConfigDialog configDialog;
    private JpcapCaptor jpcapCaptor;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane mainPane = new BorderPane();

        VBox display = new VBox();
        display.setSpacing(10);
        display.setPadding(new Insets(10, 20, 10, 20));
        // 自动换行
        taDisplay.setWrapText(true);
        // 只读
        taDisplay.setEditable(false);
        taDisplay.setPrefHeight(250);
        display.getChildren().addAll(new Label("抓包信息："), taDisplay);
        VBox.setVgrow(taDisplay, Priority.ALWAYS);
        mainPane.setCenter(display);

        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        buttons.setPadding(new Insets(10, 20, 10, 20));
        buttons.getChildren().addAll(btnStart, btnStop, btnClear, btnSetting, btnExit);
        mainPane.setBottom(buttons);
        Scene scene = new Scene(mainPane, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        btnSetting.setOnAction(event -> {
            if(configDialog == null){
                configDialog = new ConfigDialog(primaryStage);
            }
            configDialog.showAndWait();
            jpcapCaptor = configDialog.getJpcapCaptor();
        });
        btnStart.setOnAction(event -> {
            if(jpcapCaptor ==null){
                if(configDialog ==null){
                    configDialog = new ConfigDialog(primaryStage);
                }
                configDialog.showAndWait();
                jpcapCaptor = configDialog.getJpcapCaptor();
            }
            interrupt("captureThread");
            new Thread(()->{
                taDisplay.appendText("----------开始抓包----------\n");
                while (true){
                    if(Thread.currentThread().isInterrupted()){
                        break;
                    }
                    jpcapCaptor.processPacket(1,new PacketHandler());
                }
            },"captureThread").start();
        });
        btnStop.setOnAction(event -> {
            interrupt("captureThread");
            taDisplay.appendText("----------停止抓包----------\n");
        });

        btnClear.setOnAction(event -> {
            taDisplay.clear();
        });

        btnExit.setOnAction(event -> {
            exit();
        });
        primaryStage.setOnCloseRequest(event -> {
            exit();
        });
    }
    class PacketHandler implements PacketReceiver {
        @Override
        public void receivePacket(Packet packet) {
            Platform.runLater(() -> {
                taDisplay.appendText(packet.toString() + "\n");
            });
        }
    }
    private  void interrupt(String threadName){
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThread = new Thread[noThreads];
        currentGroup.enumerate(lstThread);
        for(int i=0;i<noThreads;i++){
            if(lstThread[i].getName().equals(threadName)){
            lstThread[i].interrupt();
        }
        }
    }
    private void exit()
    {
        interrupt("captureThread");
        System.exit(0);
    }
}
