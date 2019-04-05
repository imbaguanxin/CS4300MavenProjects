import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import sgraph.IScenegraphRenderer;

/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly encapsulates all our
 * OpenGL functionality from the rest of Java GUI, managed by the JOGLFrame class.
 */
public class View {

  private final float ANGLE_OF_VIEW = 120f;

  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Stack<Matrix4f> modelViewWorld;
  private Matrix4f projection, trackballTransform;
  private float aspect, trackballRadius;
  private Vector2f mousePos;
  private Vector3f cameraPosition, centerPosition;

  private util.ShaderProgram program;
  private util.ShaderLocationsVault shaderLocations;
  private int projectionLocation;
  private sgraph.IScenegraph<VertexAttrib> scenegraph;
  private AWTGLReadBufferUtil screenCaptureUtil;
  private IScenegraphRenderer renderer;

  /**
   * Construct a View object. Set up current position and rotation.
   */
  public View() {
    aspect = 1f;
    projection = new Matrix4f();
    modelViewWorld = new Stack<>();
    trackballRadius = 300;
    trackballTransform = new Matrix4f();
    scenegraph = null;
    screenCaptureUtil = null;
    // Timer here help to execute camera.
    // When it is in Ring mode, time runs and camera position is determined by time.
    // When a term in cameraFlag table is true, it execute the corresponding command.
    // Therefore, as a key pressed, the camera changes according to that key.
    // The more time it is pressed, the more changes the camera is going to make.
    cameraPosition = new Vector3f(0, 0, 500);
    centerPosition = new Vector3f(0, 0, 0);
  }

  /**
   * Set the camera position with the float array provided. The provided float array should have a
   * length of 3, representing the position of camera in 3D cartesian coordinates.
   *
   * @param position a float array representing position of camera
   */
  public void setFixedCameraPosition(float[] position) {
    if (position != null && position.length >= 3) {
      this.cameraPosition = new Vector3f(position[0], position[1], position[2]);
    }
  }

  /**
   * Set the camera position with the float array provided. The provided float array should have a
   * length of 3, representing the position of camera in 3D cartesian coordinates.
   *
   * @param position a float array representing position of camera
   */
  public void setFixedCenterPosition(float[] position) {
    if (position != null && position.length >= 3) {
      this.centerPosition = new Vector3f(position[0], position[1], position[2]);
    }
  }

  /**
   * Initialize the scene graph that is to be drawn.
   *
   * @param gla an OpenGL canvas that scene graph will be drawn upon
   * @param in an input stream of an XML file that represents the scene graph
   * @throws Exception when the input stream is not valid
   */
  public void initScenegraph(GLAutoDrawable gla, InputStream in) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    if (scenegraph != null) {
      scenegraph.dispose();
    }

    program.enable(gl);

    scenegraph = sgraph.SceneXMLReader.importScenegraph(in
        , new VertexAttribProducer());

    this.renderer = new sgraph.RayTraceRenderer();
    renderer.setContext(gla);
    Map<String, String> shaderVarsToVertexAttribs = new HashMap<>();
    shaderVarsToVertexAttribs.put("vPosition", "position");
    shaderVarsToVertexAttribs.put("vNormal", "normal");
    shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
    renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
    scenegraph.setRenderer(renderer);
    program.disable(gl);
  }

  /**
   * Initialize the shader program for drawing.
   *
   * @param gla an OpenGL canvas that scene graph will be drawn upon
   * @throws Exception when the shader is not valid
   */
  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new util.ShaderProgram();

    program.createProgram(gl, "shaders/phong-multiple.vert", "shaders/phong-multiple-spotlight"
        + ".frag");

    shaderLocations = program.getAllShaderVariables(gl);

    //get input variables that need to be given to the shader program
    projectionLocation = shaderLocations.getLocation("projection");
  }

  /**
   * This method captures the current frame buffer and writes it to a file of the given name
   *
   * @param filename the name of the file where the image should be saved
   */
  public void captureFrame(String filename, GLAutoDrawable gla) throws
      FileNotFoundException, IOException {
    if (screenCaptureUtil == null) {
      screenCaptureUtil = new AWTGLReadBufferUtil(gla.getGLProfile(), false);
    }

    File f = new File(filename);
    GL3 gl = gla.getGL().getGL3();

    BufferedImage image = screenCaptureUtil.readPixelsToBufferedImage(gl, true);
    OutputStream file = null;
    file = new FileOutputStream(filename);

    ImageIO.write(image, "png", file);

  }

  /**
   * This method will draw images on the canvas space according to the current state.
   *
   * @param gla an OpenGL canvas that scene graph will be drawn upon
   */
  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
    gl.glEnable(gl.GL_DEPTH_TEST);

    program.enable(gl);

    modelViewBuildHelper();

    /*
     *Supply the shader with all the matrices it expects.
     */
    FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
    gl.glUniformMatrix4fv(projectionLocation, 1, false, projection.get(fb));
    gl.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    scenegraph.draw(modelViewWorld);



    /*
     *OpenGL batch-processes all its OpenGL commands.
     *The next command asks OpenGL to "empty" its batch of issued commands, i.e. draw
     *
     *This a non-blocking function. That is, it will signal OpenGL to draw, but won't wait for it to
     *finish drawing.
     *
     *If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
     */
    gl.glFlush();

    program.disable(gl);
  }

  /**
   * This is a helper to build two modelView, one for Drone, one for world
   */
  private Stack<Matrix4f> modelViewBuildHelper() {
    while (!modelViewWorld.empty()) {
      modelViewWorld.pop();
    }
    modelViewWorld.push(new Matrix4f());
    modelViewWorld.peek().lookAt(
        new Vector3f(cameraPosition),
        new Vector3f(centerPosition),
        new Vector3f(0, 1, 0))//.rotate((float)Math.toRadians(1)*time, 1,0,0);
        .mul(trackballTransform);

    Stack<Matrix4f> mvCopy = new Stack<>();
    for (Matrix4f mv : modelViewWorld) {
      mvCopy.push(new Matrix4f(mv));
    }
    return mvCopy;
  }


  /**
   * Called when the mouse is pressed. Record the position where the mouse is pressed.
   *
   * @param x the x coordinate of the mouse
   * @param y the y coordinate of the mouse
   */
  public void mousePressed(int x, int y) {
    mousePos = new Vector2f(x, y);
  }

  /**
   * Called when the mouse is released.
   *
   * @param x the x coordinate of the mouse
   * @param y the y coordinate of the mouse
   */
  public void mouseReleased(int x, int y) {
  }

  /**
   * This method is called when the image is dragged by mouse, which is going to result in a
   * rotation of 3D objects showing on the screen.
   *
   * @param x the x coordinate of the mouse
   * @param y the y coordinate of the mouse
   */
  public void mouseDragged(int x, int y) {
    // When in global mode, we enables the track ball. If in Ring mode, we do nothing
    // If the main canvas is in drone mode, we also disable the track ball
    Vector2f newM = new Vector2f(x, y);

    Vector2f delta = new Vector2f(newM.x - mousePos.x, newM.y - mousePos.y);
    mousePos = new Vector2f(newM);

    trackballTransform = new Matrix4f().rotate(delta.x / trackballRadius, 0, 1, 0)
        .rotate(delta.y / trackballRadius, 1, 0, 0)
        .mul(trackballTransform);

  }

  /**
   * This method handles the key input. It changes the cameraFlag table, which is run by a timer.
   * The timer look at the cameraFlag table every 10 ms. If the timer sees a Flag is true, then it
   * ask the camera to do corresponding job.
   *
   * @param e This is a KeyEvent from user.
   * @param pressed This is a boolean representing whether it is pressed or released. True means
   * this key is pressed, False means this key is released
   */
  void keyAction(KeyEvent e, boolean pressed) {
    switch (e.getKeyCode()) {
      // Other keys should not detected as not available
      case KeyEvent.VK_SPACE:
        System.out.println("Start ray trace!");
        scenegraph.rayTrace(WINDOW_WIDTH, WINDOW_HEIGHT, modelViewBuildHelper(), ANGLE_OF_VIEW);
        System.out.println("完事了！");
        break;
      case KeyEvent.VK_SHIFT | KeyEvent.VK_G | KeyEvent.VK_T:
        break;
      default:
        System.out.println("KeyEvent KeyCode: " + e.getKeyCode());
    }
  }

  /**
   * This method will be called each time the window is reshaped and the content will be resized
   * properly to maintain an appropriate proportion.
   *
   * @param gla the canvas that we will be drawing on
   * @param width the new width of window
   * @param height the new height of window
   */
  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    aspect = (float) width / height;
    projection = new Matrix4f()
        .perspective((float) Math.toRadians(ANGLE_OF_VIEW), aspect, 0.1f, 10000.0f);
    // proj = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);

  }

  /**
   * This method is called when the window is closed and will cleanup all the ObjectInstances.
   *
   * @param gla the canvas that we will be drawing on
   */
  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();
  }

}


