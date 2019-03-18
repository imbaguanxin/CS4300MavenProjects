import javax.swing.*;

/**
 * Created by ashesh on 10/30/2015.
 */
public class EnhancedCamera {

  public static void main(String[] args) {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI(args[0]);
      }
    });
  }

  private static void createAndShowGUI(String configPath) {
    JFrame frame = new JOGLFrame("Scene graph Viewer", configPath);
    frame.setVisible(true);
  }
}
