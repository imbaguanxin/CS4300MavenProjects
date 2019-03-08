import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.joml.Vector4f;
import sgraph.IScenegraph;
import sgraph.RotateScenegraph;
import sgraph.Scenegraph;

/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly encapsulates all our
 * OpenGL functionality from the rest of Java GUI, managed by the JOGLFrame class.
 */
public class View {

  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Timer timer;
  private int time;
  private Stack<Matrix4f> modelViewDrone, modelViewWorld;
  private Matrix4f projection, trackballTransform;
  private float trackballRadius;
  private Vector2f mousePos;
  private Vector3f cameraPosition, centerPosition;
  private Camera moving_camera;
  private boolean[] cameraFlags;
  private boolean isDroneMode;

  private util.ShaderProgram program;
  private util.ShaderLocationsVault shaderLocations;
  private int projectionLocation;
  private sgraph.IScenegraph<VertexAttrib> scenegraph;


  /**
   * Construct a View object. Set up current position and rotation.
   */
  public View() {
    time = 0;
    projection = new Matrix4f();
    modelViewDrone = new Stack<>();
    modelViewWorld = new Stack<>();
    trackballRadius = 300;
    trackballTransform = new Matrix4f();
    scenegraph = null;
    timer = new Timer(true);
    cameraFlags = new boolean[10];
    moving_camera = new Camera();
    // Timer here help to execute camera.
    // When it is in Ring mode, time runs and camera position is determined by time.
    // When a term in cameraFlag table is true, it execute the corresponding command.
    // Therefore, as a key pressed, the camera changes according to that key.
    // The more time it is pressed, the more changes the camera is going to make.
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        time++;
        for (int i = 0; i < cameraFlags.length; i++) {
          if (cameraFlags[i]) {
            moving_camera.executeCamera(i);
          }
        }
      }
    }, 10, 10);
    cameraPosition = new Vector3f(0, 0, 500);
    centerPosition = new Vector3f(0, 0, 0);
    isDroneMode = false;
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

  public void setDroneMode() {
    this.isDroneMode = !isDroneMode;
  }

  void initMovingCamera(float[] position, float[] center, GLAutoDrawable glAutoDrawable)
      throws Exception {
    if (position == null | center == null) {
      throw new IllegalArgumentException("Input array is empty when initializing moving camera!");
    }
    if (position.length < 3 | center.length < 3) {
      throw new IllegalArgumentException(
          "Input array is too short when initializing moving camera!");
    }
    this.moving_camera.initCamera(
        new Vector3f(position[0], position[1], position[2]),
        new Vector3f(center[0], center[1], center[2]),
        glAutoDrawable);
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

    sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
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

    program.createProgram(gl, "shaders/default.vert", "shaders/default"
        + ".frag");

    shaderLocations = program.getAllShaderVariables(gl);

    //get input variables that need to be given to the shader program
    projectionLocation = shaderLocations.getLocation("projection");
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
    if (isDroneMode) {
      scenegraph.draw(modelViewDrone);
    } else {
      scenegraph.draw(modelViewWorld);
      moving_camera.draw(modelViewWorld);
    }
    gl.glScissor(WINDOW_WIDTH / 3 * 2, WINDOW_HEIGHT / 3 * 2, WINDOW_WIDTH / 3,
        WINDOW_HEIGHT / 3);
    gl.glEnable(gl.GL_SCISSOR_TEST);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
    gl.glViewport(WINDOW_WIDTH / 3 * 2, WINDOW_HEIGHT / 3 * 2, WINDOW_WIDTH / 3,
        WINDOW_HEIGHT / 3);
    if (isDroneMode) {
      scenegraph.draw(modelViewWorld);
      moving_camera.draw(modelViewWorld);
    } else {
      scenegraph.draw(modelViewDrone);
    }
    gl.glDisable(gl.GL_SCISSOR_TEST);

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
  private void modelViewBuildHelper() {
    // pop all the items in modeViews
    while (!modelViewWorld.empty()) {
      modelViewWorld.pop();
    }
    while (!modelViewDrone.empty()) {
      modelViewDrone.pop();
    }

    /*
     * In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
     * We use a modelview matrix to store the transformations to be applied to our triangle.
     * The state of our modelview matrix will depend on the state of the camera, a global or a turntable camera.
     */

    modelViewWorld.push(new Matrix4f());
    modelViewDrone.push(new Matrix4f());

    modelViewDrone.peek().mul(moving_camera.getLookat());
    modelViewWorld.peek().lookAt(
        new Vector3f(cameraPosition),
        new Vector3f(centerPosition),
        new Vector3f(0, 1, 0))
        .mul(trackballTransform);
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
    if (!isDroneMode) {
      Vector2f newM = new Vector2f(x, y);

      Vector2f delta = new Vector2f(newM.x - mousePos.x, newM.y - mousePos.y);
      mousePos = new Vector2f(newM);

      trackballTransform = new Matrix4f().rotate(delta.x / trackballRadius, 0, 1, 0)
          .rotate(delta.y / trackballRadius, 1, 0, 0)
          .mul(trackballTransform);
    }
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
    int keyCode = e.getKeyCode();
    switch (keyCode) {
      case KeyEvent.VK_UP:
        this.cameraFlags[0] = pressed;
        break;
      case KeyEvent.VK_DOWN:
        this.cameraFlags[1] = pressed;
        break;
      case KeyEvent.VK_RIGHT:
        this.cameraFlags[2] = pressed;
        break;
      case KeyEvent.VK_LEFT:
        this.cameraFlags[3] = pressed;
        break;
      case KeyEvent.VK_W:
        this.cameraFlags[4] = pressed;
        break;
      case KeyEvent.VK_S:
        this.cameraFlags[5] = pressed;
        break;
      case KeyEvent.VK_D:
        this.cameraFlags[6] = pressed;
        break;
      case KeyEvent.VK_A:
        this.cameraFlags[7] = pressed;
        break;
      case KeyEvent.VK_F:
        this.cameraFlags[8] = pressed;
        break;
      case KeyEvent.VK_C:
        this.cameraFlags[9] = pressed;
        break;
      case KeyEvent.VK_SPACE:
        break;
      case KeyEvent.VK_G:
        break;
      case KeyEvent.VK_T:
        break;
      default:
        System.out.println("This key is not available");
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
    projection = new Matrix4f()
        .perspective((float) Math.toRadians(120.0f), (float) width / height, 0.1f, 10000.0f);
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


  /**
   * This is a class representing the drone camera.
   */
  private class Camera {

    // These are constant numbers representing distinct actions on camera.
    final int POSITION_UP = 0;
    final int POSITION_DOWN = 1;
    final int POSITION_RIGHT = 2;
    final int POSITION_LEFT = 3;
    final int DIRECTION_UP = 4;
    final int DIRECTION_DOWN = 5;
    final int DIRECTION_RIGHT = 6;
    final int DIRECTION_LEFT = 7;
    final int ROTATE_CLOCK_WISE = 8;
    final int ROTATE_COUNTER_CLOCK_WISE = 9;

    final private Vector4f UP = new Vector4f(0, 1, 0, 0);
    final private Vector4f DIRECTION = new Vector4f(0, 0, -1, 0);
    private Vector4f position;
    // trackBall records the camera's posture.
    private Matrix4f trackBall;
    sgraph.IScenegraph<VertexAttrib> camera_scenegraph, drone_scenegraph;
    List<IScenegraph<VertexAttrib>> propellers;

    // Building a camera, which need initialization.
    Camera() {
      this.position = new Vector4f();
      this.trackBall = new Matrix4f().identity();
      propellers = new ArrayList<>();
    }

    /**
     * Initialize a camera.
     *
     * @param position The position of the camera in world coordinate system.(Same as the first
     * parameter in lookat matrix)
     * @param center The point that this camera is looking at.(Same as the second parameter in
     * lookat matrix)
     * @param gla Used for draw the camera in world.
     */
    void initCamera(Vector3f position, Vector3f center, GLAutoDrawable gla) throws Exception {
      String camera_input = "scenegraphmodels/camera.xml";
      String drone_input = "scenegraphmodels/drone.xml";
      String propeller_input = "scenegraphmodels/propeller.xml";
      InputStream camera_in = getClass().getClassLoader().getResourceAsStream(camera_input);
      InputStream drone_in = getClass().getClassLoader().getResourceAsStream(drone_input);
      InputStream propeller_in = getClass().getClassLoader().getResourceAsStream(propeller_input);
      this.position = new Vector4f(position.x, position.y, position.z, 1);
      this.trackBall = new Matrix4f().identity().lookAt(
          new Vector3f(0, 0, 0),
          new Vector3f(center.x - position.x, center.y - position.y,
              center.z - position.z).normalize(), new Vector3f(0, 1, 0));
      GL3 gl = gla.getGL().getGL3();

      if (camera_scenegraph != null) {
        camera_scenegraph.dispose();
      }
      if (drone_scenegraph != null) {
        drone_scenegraph.dispose();
      }
      for (IScenegraph prop : propellers) {
        if (prop != null) {
          prop.dispose();
        }
      }

      program.enable(gl);
      camera_scenegraph = sgraph.SceneXMLReader
          .importScenegraph(camera_in, new VertexAttribProducer());
      drone_scenegraph = sgraph.SceneXMLReader
          .importScenegraph(drone_in, new VertexAttribProducer());

      IScenegraph propeller = sgraph.SceneXMLReader
          .importScenegraph(propeller_in, new VertexAttribProducer());
      float adder = 14 / (float) Math.sqrt(2);

      propellers.add(new RotateScenegraph<>(propeller, new Vector3f(0, 1, 0), 10f,
          new Vector3f(4 + adder, 7.5f, 3 + adder)));
      propellers.add(new RotateScenegraph<>(propeller, new Vector3f(0, 1, 0), 10f,
          new Vector3f(-(4 + adder), 7.75f, 3 + adder)));
      propellers.add(new RotateScenegraph<>(propeller, new Vector3f(0, 1, 0), 10f,
          new Vector3f(4 + adder, 7.25f, -(3 + adder))));
      propellers.add(new RotateScenegraph<>(propeller, new Vector3f(0, 1, 0), 10f,
          new Vector3f(-(4 + adder), 7.25f, -(3 + adder))));
      sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
      renderer.setContext(gla);
      Map<String, String> shaderVarsToVertexAttribs = new HashMap<>();
      shaderVarsToVertexAttribs.put("vPosition", "position");
      shaderVarsToVertexAttribs.put("vNormal", "normal");
      shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
      renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
      camera_scenegraph.setRenderer(renderer);
      drone_scenegraph.setRenderer(renderer);
      for (IScenegraph prop : propellers) {
        prop.setRenderer(renderer);
      }
      program.disable(gl);
    }

    /**
     * It returns a look at matrix of the camera. This lookAt matrix shows where the camera is in
     * the world and where is it looking at
     */
    Matrix4f getLookat() {
      Vector4f up = trackBall.transform(new Vector4f(UP));
      Vector4f center = new Vector4f(DIRECTION).mul(trackBall).add(position);
      return new Matrix4f().identity().lookAt(
          new Vector3f(position.x, position.y, position.z),
          new Vector3f(center.x, center.y, center.z),
          new Vector3f(up.x, up.y, up.z));
    }

    /**
     * This function adjust the posture of the camera by matrix multiplication.
     *
     * @param arg arg shows what to do.
     */
    void executeCamera(final int arg) throws IllegalArgumentException {
      float phi = (float) Math.PI / 120f;
      Vector4f right = trackBall.transform(new Vector4f(1, 0, 0, 0));
      Vector4f up = trackBall.transform(new Vector4f(UP));
      Vector4f tempDir = trackBall.transform(new Vector4f(DIRECTION));
      switch (arg) {
        case POSITION_UP:
          position.add(trackBall.transform(new Vector4f(UP)));
          break;
        case POSITION_DOWN:
          position.sub(trackBall.transform(new Vector4f(UP)));
          break;
        case POSITION_RIGHT:
          position.add(right);
          break;
        case POSITION_LEFT:
          position.sub(right);
          break;
        case DIRECTION_UP:
          trackBall = new Matrix4f().identity().rotate(phi, right.x, right.y, right.z)
              .mul(trackBall);
          break;
        case DIRECTION_DOWN:
          trackBall = new Matrix4f().identity().rotate(-phi, right.x, right.y, right.z)
              .mul(trackBall);
          break;
        case DIRECTION_RIGHT:
          trackBall = new Matrix4f().identity().rotate(-phi, up.x, up.y, up.z).mul(trackBall);
          break;
        case DIRECTION_LEFT:
          trackBall = new Matrix4f().identity().rotate(phi, up.x, up.y, up.z).mul(trackBall);
          break;
        case ROTATE_CLOCK_WISE:
          trackBall = new Matrix4f().identity().rotate(phi, tempDir.x, tempDir.y, tempDir.z)
              .mul(trackBall);
          break;
        case ROTATE_COUNTER_CLOCK_WISE:
          trackBall = new Matrix4f().identity().rotate(-phi, tempDir.x, tempDir.y, tempDir.z)
              .mul(trackBall);
          break;
        default:
          throw new IllegalArgumentException("Unexpected Execution Command!");
      }
    }

    /**
     * This method draw the camera in the world.
     *
     * @param passedInModelView The passed in modelView of world.
     */
    void draw(Stack<Matrix4f> passedInModelView) {
      //System.out.println(time);
      passedInModelView.push(new Matrix4f(passedInModelView.peek()));
      passedInModelView.peek().
          translate(position.x, position.y, position.z);
      drone_scenegraph.draw(passedInModelView);

      passedInModelView.push(new Matrix4f(passedInModelView.peek()));
      passedInModelView.peek().
          mul(trackBall);
      camera_scenegraph.draw(passedInModelView);
      passedInModelView.pop();

      for(IScenegraph prop: propellers){
        prop.animate(time);
        prop.draw(passedInModelView);
      }

      passedInModelView.pop();
    }
  }
}


