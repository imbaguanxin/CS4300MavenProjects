package sgraph;

import java.util.Map;
import java.util.Stack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import util.IVertexData;
import util.Light;
import util.PolygonMesh;

/**
 * A specific implementation of this scene graph. This implementation is still independent of the
 * rendering technology (i.e. OpenGL). This class now support animations by calling the method
 * animate(float time).
 */
public class RotateScenegraph<VertexType extends IVertexData> implements IScenegraph<VertexType> {

  private IScenegraph originalScenegraph;
  private Vector3f axis;
  private Matrix4f rotateMatrix, translateMatrix;
  private float angle;

  public RotateScenegraph(IScenegraph sg, Vector3f axis, float angularSpeed, Vector3f position) {
    this.originalScenegraph = sg;
    this.axis = axis;
    this.rotateMatrix = new Matrix4f().identity();
    this.angle = (float) Math.toRadians(angularSpeed);
    this.translateMatrix = new Matrix4f().identity().translate(position.x, position.y, position.z);
  }

  public RotateScenegraph(IScenegraph sg, Vector3f axis, float angularSpeed) {
    this(sg, axis, angularSpeed, new Vector3f(0, 0, 0));
  }

  /**
   * Sets the renderer, and then adds all the meshes to the renderer. This function must be called
   * when the scene graph is complete, otherwise not all of its meshes will be known to the
   * renderer
   *
   * @param renderer The {@link IScenegraphRenderer} object that will act as its renderer
   */
  public void setRenderer(IScenegraphRenderer renderer) throws Exception {
    originalScenegraph.setRenderer(renderer);
  }

  /**
   * initialize the supplied root to the be the root of this scene graph. This is supposed to
   * overwrite any previous roots
   */
  public void makeScenegraph(INode root) {
    originalScenegraph.makeScenegraph(root);
  }

  /**
   * Draw this scene graph, using the stack of modelview matrices provided. The scene graph will use
   * this stack as it navigates its tree.
   */
  public void draw(Stack<Matrix4f> modelView) {
    modelView.push(new Matrix4f(modelView.peek()));
    modelView.peek().mul(translateMatrix).mul(rotateMatrix);
    originalScenegraph.draw(modelView);
    modelView.pop();
  }

  @Override
  public void lightOn(Stack<Matrix4f> modelView) {
    originalScenegraph.lightOn(modelView);
  }

  /**
   * Add a polygon mesh that will be used by one or more leaves in this scene graph
   *
   * @param name a unique name by which this mesh may be referred to in future
   * @param obj the {@link util.PolygonMesh} object
   */
  public void addPolygonMesh(String name, util.PolygonMesh<VertexType> obj) {
    originalScenegraph.addPolygonMesh(name, obj);
  }

  /**
   * Specific scene graph implementations should put code that animates specific nodes in the scene
   * graph, based on a time provided by the caller
   *
   * @param time provides a simple time reference for animation
   */
  public void animate(float time) {
    rotateMatrix = rotateMatrix.identity().rotate(angle * time, axis.x, axis.y, axis.z);
  }

  /**
   * Adds a node to itself. This should be stored in a suitable manner by an implementation, so that
   * it is possible to look up a specific node by name
   *
   * @param name (hopefully unique) name given to this node
   * @param node the node object
   */
  public void addNode(String name, INode node) {
    originalScenegraph.addNode(name, node);
  }

  /**
   * Get the root of this scene graph
   *
   * @return the root of this scene graph
   */
  public INode getRoot() {
    return originalScenegraph.getRoot();
  }

  /**
   * Get a mapping of all (name,mesh) pairs that have been added to this scene graph This function
   * is useful in case all meshes of one scene graph have to be added to another in an attempt to
   * merge two scene graphs
   */
  public Map<String, PolygonMesh<VertexType>> getPolygonMeshes() {
    return originalScenegraph.getPolygonMeshes();
  }


  /**
   * Get a mapping of all (name,INode) pairs for all nodes in this scene graph. This function is
   * useful in case all meshes of one scene graph have to be added to another in an attempt to merge
   * two scene graphs
   */
  public Map<String, INode> getNodes() {
    return originalScenegraph.getNodes();
  }

  /**
   * Add a new texture by this name
   */
  public void addTexture(String name, String path) {
    originalScenegraph.addTexture(name, path);
  }

  public void dispose() {
    originalScenegraph.dispose();
  }
}
