package sgraph;

import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import util.Light;

import java.util.Stack;

/**
 * This interface represents all the operations offered by any type of node in our scenegraph. Not
 * all types of nodes are able to offer all types of operations. This is implemented by the {@link
 * sgraph.AbstractNode} throwing an exception for all such methods, and appropriate nodes overriding
 * these methods
 *
 * @author Amit Shesh
 */
public interface INode {

  /**
   * In the scene graph rooted at this node, get the node whose name is as given
   *
   * @param name name of node to be searched
   * @return the node reference if it exists, null otherwise
   */
  INode getNode(String name);

  /**
   * Draw the scene graph rooted at this node, using the modelview stack and context
   *
   * @param context the generic renderer context {@link sgraph.IScenegraphRenderer}
   * @param modelView the stack of modelview matrices
   */
  void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView);

  void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView,
      Map<Light, Matrix4f> passedInlights);

  /**
   * Return a deep copy of the scene graph subtree rooted at this node
   *
   * @return a reference to the root of the copied subtree
   */
  public INode clone();

  /**
   * Set the parent of this node. Each node except the root has a parent
   *
   * @param parent the node that is to be the parent of this node
   */
  void setParent(INode parent);

  /**
   * Traverse the scene graph rooted at this node, and store references to the scenegraph object
   *
   * @param graph a reference to the scenegraph object of which this tree is a part
   */

  void setScenegraph(IScenegraph graph);

  /**
   * Set the name of this node. The name is not guaranteed to be unique in the tree, but it should
   * be.
   *
   * @param name the name of this node
   */
  void setName(String name);


  /**
   * Get the name of this node
   *
   * @return the name of this node
   */
  String getName();

  /**
   * Add a child to this node. Not all types of nodes have the capability of having children. If the
   * node cannot have a child, this method throws an {@link }IllegalArgumentException}
   *
   * @param node the node that must be added as a child to this node
   * @throws {@link }IllegalArgumentException} if this node is unable to have children (i.e.
   * leaves)
   */
  void addChild(INode node) throws IllegalArgumentException;

  /**
   * Set the transformation associated with this node. Not all types of nodes can have
   * transformations. If the node cannot store a transformation, this method throws an {@link
   * }IllegalArgumentException}
   *
   * @param m the tranformation matrix associated with this transformation
   * @throws {@link }IllegalArgumentException} if this node is unable to store a transformation (all
   * nodes except TransformNode)
   */
  void setTransform(Matrix4f m) throws IllegalArgumentException;


  /**
   * Set the animation transformation associated with this node. Not all types of nodes can have
   * transformations. If the node cannot store an animation transformation, this method throws an
   * {@link }IllegalArgumentException}
   *
   * @param m the animation tranformation matrix associated with this node
   * @throws {@link }IllegalArgumentException} if this node is unable to store a transformation (all
   * nodes except TransformNode)
   */
  void setAnimationTransform(Matrix4f m) throws IllegalArgumentException;


  /**
   * Set the material associated with this node. Not all types of nodes can have materials
   * associated with them. If the node cannot have a material, this method throws an {@link
   * }IllegalArgumentException}
   *
   * @param m the material object to be associated with this node
   * @throws {@link }IllegalArgumentException} if this node is unable to store a material (all nodes
   * except leaves)
   */
  void setMaterial(util.Material m) throws IllegalArgumentException;

  /**
   * Sets the texture to be associated with this node. Not all types of nodes can have textures
   * associated with them. If the node cannot have a texture, this methods throws an {@link
   * IllegalArgumentException}.
   */
  void setTextureName(String name) throws IllegalArgumentException;

  /**
   * Adds a new light to this node.
   */
  void addLight(Light l);
}

