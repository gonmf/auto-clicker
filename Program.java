import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Program{

  public static void main(String[] args) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new Program().prepareGUI();
      }
    });
  }
  private JFrame mainFrame;
  private Button bStart;
  private Button bStop;
  private Label lCurrValueX;
  private Label lCurrValueY;
  private TextField tValueX;
  private TextField tValueY;
  private TextField tInterval;
  private Robot robo;
  private Object lock = new Object();
  private boolean mouseListenerEnabled = false;
  private boolean mouseRobotEnabled = false;

  private void prepareGUI() {
    try {
      robo = new Robot();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    mainFrame = new JFrame("Auto Clicker");
    mainFrame.setSize(200, 180);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    mainFrame.setResizable(false);

    int currValueX = trunc(MouseInfo.getPointerInfo().getLocation().x);
    int currValueY = trunc(MouseInfo.getPointerInfo().getLocation().y);

    lCurrValueX = new Label("X: " + currValueX);
    lCurrValueY = new Label("Y: " + currValueY);
    Label lValues = new Label("Input coordinates bellow");
    tValueX = new TextField(currValueX + "");
    tValueY = new TextField(currValueY + "");
    Label lInterval = new Label("Interval (ms): ");
    tInterval = new TextField("1000");
    bStart = new Button("Start");
    bStop = new Button("Stop");

    JPanel topPanel = new JPanel(new GridLayout(1, 2));
    JPanel midPanel = new JPanel(new GridLayout(1, 2));
    JPanel botPanel = new JPanel(new GridLayout(1, 2));

    topPanel.add(lCurrValueX);
    topPanel.add(lCurrValueY);
    midPanel.add(tValueX);
    midPanel.add(tValueY);
    botPanel.add(lInterval);
    botPanel.add(tInterval);

    mainFrame.add(topPanel);
    mainFrame.add(lValues);
    mainFrame.add(midPanel);
    mainFrame.add(botPanel);
    mainFrame.add(bStart);
    mainFrame.add(bStop);

    bStart.addActionListener(new StartListener());
    bStop.addActionListener(new StopListener());
    bStart.setEnabled(true);
    bStop.setEnabled(false);
    mainFrame.setVisible(true);

    launchActiveMouseListener();
  }

  private int trunc(int val) {
    return val > 99 ? val / 10 * 10 : val;
  }

  private void launchActiveMouseListener() {
    if (mouseListenerEnabled) return;

    mouseListenerEnabled = true;

    Thread activeMouseMotionListener = new Thread(new Runnable() {
      @Override
      public void run() {
        try{
          while (true) {
            if (!mouseListenerEnabled) return;

            Point p = MouseInfo.getPointerInfo().getLocation();
            lCurrValueX.setText("X: " + p.x);
            lCurrValueY.setText("Y: " + p.y);
            Thread.sleep(50);
          }
        } catch(Exception e) {}
      }
    });
    activeMouseMotionListener.start();
  }

  private class StartListener implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent ae) {
      final int x;
      final int y;
      final int interval;

      try{
        x = Integer.parseUnsignedInt(tValueX.getText());
      }catch(Exception e) {
        return;
      }
      try{
        y = Integer.parseUnsignedInt(tValueY.getText());
      }catch(Exception e) {
        return;
      }
      try{
        interval = Integer.parseUnsignedInt(tInterval.getText());
        assert(interval > 9);
      }catch(Exception e) {
        return;
      }

      synchronized(lock) {
        if (!bStart.isEnabled()) return;

        bStart.setEnabled(false);
        lCurrValueX.setEnabled(false);
        lCurrValueY.setEnabled(false);
        tValueX.setEnabled(false);
        tValueY.setEnabled(false);
        tInterval.setEnabled(false);

        mouseListenerEnabled = false;
        mouseRobotEnabled = true;

        Thread tSimul = new Thread(new Runnable() {
          @Override
          public void run() {
            try{
              while(true) {
                if (!mouseRobotEnabled) return;

                Point pev = MouseInfo.getPointerInfo().getLocation();
                if (pev.x != x && pev.y != y) {
                  robo.mouseMove(x, y);
                }
                robo.mousePress(InputEvent.BUTTON1_MASK);
                robo.mouseRelease(InputEvent.BUTTON1_MASK);
                if (pev.x != x && pev.y != y) {
                  robo.mouseMove(pev.x, pev.y);
                }
                Thread.sleep(interval);
              }
            }catch(Exception e) {}
          }
        });
        tSimul.start();

        robo.mouseMove(x, y);

        bStop.setEnabled(true);
      }
    }
  }

  private class StopListener implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent ae) {
      synchronized(lock) {
        if (!bStop.isEnabled()) return;

        bStop.setEnabled(false);

        mouseRobotEnabled = false;

        launchActiveMouseListener();

        lCurrValueX.setEnabled(true);
        lCurrValueY.setEnabled(true);
        tValueX.setEnabled(true);
        tValueY.setEnabled(true);
        tInterval.setEnabled(true);
        bStart.setEnabled(true);
      }
    }
  }
}
