import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;

import java.io.FileInputStream;
import java.util.Scanner;
import javax.swing.*;

import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ashesh on 9/18/2015.
 */
public class JOGLFrame extends JFrame {

  private View view;
  private GLCanvas canvas;
  private int frameno;

  public JOGLFrame(String title, String configPath) {
    //routine JFrame setting stuff
    super(title);
    setSize(500, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when X is pressed, close program

    //Our View class is the actual driver of the OpenGL stuff
    view = new View();
    ConfigurationReader configReader = new ConfigurationReader(configPath);

    GLProfile glp = GLProfile.getMaxProgrammable(true);
    GLCapabilities caps = new GLCapabilities(glp);
    caps.setDepthBits(24);
    canvas = new GLCanvas(caps);

    add(canvas);

    //capture mouse events
    MyMouseAdapter mouseAdapter = new MyMouseAdapter();

    canvas.addMouseListener(mouseAdapter);
    canvas.addMouseMotionListener(mouseAdapter);

    canvas.addGLEventListener(new GLEventListener() {
      @Override
      public void init(
          GLAutoDrawable glAutoDrawable) { //called the first time this canvas is created. Do your initialization here
        try {
          view.init(glAutoDrawable);
          configReader.applyConfig(view, glAutoDrawable);

          glAutoDrawable.getGL().setSwapInterval(0);

        } catch (Exception e) {
          JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading",
              JOptionPane.ERROR_MESSAGE);
          System.exit(1);
        }
      }

      @Override
      public void dispose(GLAutoDrawable glAutoDrawable) { //called when the canvas is destroyed.
        view.dispose(glAutoDrawable);
      }

      @Override
      public void display(
          GLAutoDrawable glAutoDrawable) { //called every time this window must be redrawn
        view.draw(canvas);
        //capturePhoto(1000, glAutoDrawable);
      }

      void capturePhoto(int maxNum, GLAutoDrawable glAutoDrawable) {
        if (frameno < maxNum) {
          String filename = "output/image"
              + String.format("%03d.png", frameno);
          try {
            view.captureFrame(filename, glAutoDrawable);
          } catch (Exception e) {
            JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
          }
          frameno++;
        }
      }

      @Override
      public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width,
          int height) { //called every time this canvas is resized
        view.reshape(glAutoDrawable, x, y, width, height);
        repaint(); //refresh window
      }
    });

    canvas.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyPressed(KeyEvent e) {
        view.keyAction(e, true);
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          view.setDroneMode();
        } else {
          view.keyAction(e, false);
        }
      }
    });

    //Add an animator to the canvas
    AnimatorBase animator = new FPSAnimator(canvas, 60);
    animator.setUpdateFPSFrames(50, null);
    animator.start();
  }

  private class MyMouseAdapter extends MouseAdapter {

    @Override
    public void mousePressed(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
        JOGLFrame.this.view.mousePressed(e.getX(), e.getY());
      }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
        JOGLFrame.this.view.mouseReleased(e.getX(), e.getY());
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      JOGLFrame.this.view.mouseDragged(e.getX(), e.getY());
      JOGLFrame.this.canvas.repaint();
    }
  }

  /**
   * This class represents a configuration file reader that can read the configuration file provided
   * by command line argument. Configurations read from the config file can be applied to an
   * initialized view.
   */
  private class ConfigurationReader {

    private String objectPath;
    private float[] moving_cameraPosition, moving_centerPosition,
        fixed_cameraPosition, fixed_centerPosition;
    private boolean isDroneMode;

    /**
     * Construct a ConfigurationReader class that read the config file.
     *
     * @param configPath path of the configuration file
     */
    ConfigurationReader(String configPath) {
      this.moving_cameraPosition = new float[]{0, 0, 500};
      this.moving_centerPosition = new float[]{0, 0, 0};
      this.fixed_cameraPosition = new float[]{0, 0, 500, 0};
      this.fixed_centerPosition = new float[]{0, 0, 0, 0};

      Scanner scanner;
      try {
        scanner = new Scanner(new FileInputStream(configPath));
      } catch (IOException e) {
        throw new IllegalArgumentException(e.getMessage());
      }

      while (scanner.hasNext()) {
        String attribution = scanner.next().toLowerCase();
        switch (attribution) {
          case "path":
            this.objectPath = scanner.nextLine().trim();
            break;
          case "mode":
            String mode = scanner.next().toLowerCase();
            this.isDroneMode = "moving".equals(mode);
            break;
          case "move-position":
            moving_cameraPosition[0] = scanner.nextFloat();
            moving_cameraPosition[1] = scanner.nextFloat();
            moving_cameraPosition[2] = scanner.nextFloat();
            if (Math.abs(fixed_cameraPosition[3]) < 0.00001) {
              fixed_cameraPosition[0] = moving_cameraPosition[0];
              fixed_cameraPosition[1] = moving_cameraPosition[1];
              if (moving_cameraPosition[2] > 0) {
                fixed_cameraPosition[2] = moving_cameraPosition[2] - 3;
              } else {
                fixed_cameraPosition[2] = moving_cameraPosition[2] + 3;
              }
            }
            break;
          case "move-center":
            moving_centerPosition[0] = scanner.nextFloat();
            moving_centerPosition[1] = scanner.nextFloat();
            moving_centerPosition[2] = scanner.nextFloat();
            if (Math.abs(fixed_centerPosition[3] - 0) < .0001) {
              fixed_centerPosition[0] = moving_centerPosition[0];
              fixed_centerPosition[1] = moving_centerPosition[1];
              fixed_centerPosition[2] = moving_centerPosition[2];
            }
            break;
          case "fix-position":
            fixed_cameraPosition[0] = scanner.nextFloat();
            fixed_cameraPosition[1] = scanner.nextFloat();
            fixed_cameraPosition[2] = scanner.nextFloat();
            fixed_cameraPosition[3] = 1f;
            break;
          case "fix-center":
            fixed_centerPosition[0] = scanner.nextFloat();
            fixed_centerPosition[1] = scanner.nextFloat();
            fixed_centerPosition[2] = scanner.nextFloat();
            fixed_centerPosition[3] = 1f;
        }
      }
    }

    /**
     * Apply the configuration stored in this class, including to a view with a given canvas.
     *
     * @param view the view which configuration will be applied to
     * @param glAutoDrawable the canvas that the scene graph will be initialized
     * @throws Exception when the scene graph path is not valid
     */
    void applyConfig(View view, GLAutoDrawable glAutoDrawable) throws Exception {
      InputStream in = getClass().getClassLoader().getResourceAsStream(objectPath);
//      InputStream in = getClass().getClassLoader().getResourceAsStream("scenegraphmodels/cone.xml");
      System.out.println(objectPath);
      view.initScenegraph(glAutoDrawable, in);
      float[] moving_camera = new float[3];
      float[] moving_center = new float[3];
      float[] fixed_camera = new float[3];
      float[] fixed_center = new float[3];
      System.arraycopy(moving_cameraPosition, 0, moving_camera, 0, moving_camera.length);
      System.arraycopy(moving_centerPosition, 0, moving_center, 0, moving_center.length);
      System.arraycopy(fixed_cameraPosition, 0, fixed_camera, 0, fixed_camera.length);
      System.arraycopy(fixed_centerPosition, 0, fixed_center, 0, fixed_center.length);
      view.setFixedCameraPosition(fixed_cameraPosition);
      view.setFixedCenterPosition(fixed_centerPosition);
      view.initMovingCamera(moving_cameraPosition, moving_centerPosition, glAutoDrawable);
      if (this.isDroneMode) {
        view.setDroneMode();
      }
    }
  }
}
