package sgraph;

import org.joml.Matrix4f;
import java.util.Stack;

/**
 * This node represents the leaf of a scene graph. It is the only type of node that has actual
 * geometry to render.
 *
 * @author Amit Shesh
 */
public class LeafNode extends AbstractNode {

  /**
   * The name of the object instance that this leaf contains. All object instances are stored in the
   * scene graph itself, so that an instance can be reused in several leaves
   */
  protected String objInstanceName;
  /**
   * The material associated with the object instance at this leaf
   */
  protected util.Material material;

  protected String textureName;

  public LeafNode(String instanceOf, IScenegraph graph, String name) {
    super(graph, name);
    this.objInstanceName = instanceOf;
  }


  /*
   *Set the material of each vertex in this object
   */
  @Override
  public void setMaterial(util.Material mat) {
    material = new util.Material(mat);
  }

  /**
   * Set texture ID of the texture to be used for this leaf
   */
  @Override
  public void setTextureName(String name) {
    textureName = name;
  }

  /*
   * gets the material
   */
  public util.Material getMaterial() {
    return material;
  }

  @Override
  public INode clone() {
    LeafNode newclone = new LeafNode(this.objInstanceName, scenegraph, name);
    newclone.setMaterial(this.getMaterial());
    return newclone;
  }


  /**
   * Delegates to the scene graph for rendering. This has two advantages:
   * <ul>
   * <li>It keeps the leaf light.</li>
   * <li>It abstracts the actual drawing to the specific implementation of the scene graph
   * renderer</li>
   * </ul>
   *
   * @param context the generic renderer context {@link sgraph.IScenegraphRenderer}
   * @param modelView the stack of modelview matrices
   */
  @Override
  public void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView)
      throws IllegalArgumentException {
    if (objInstanceName.length() > 0) {
      context.drawMesh(objInstanceName, material, textureName, modelView.peek());
    }
  }


}
