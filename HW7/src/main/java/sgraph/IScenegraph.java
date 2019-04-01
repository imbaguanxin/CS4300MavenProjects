package sgraph;

import org.joml.Matrix4f;
import util.IVertexData;

import java.util.Map;
import java.util.Stack;

/**
 * This interface captures all the operations that a scene graph should offer. It is designed to be
 * a generic scene graph that is independent of the actual rendering library It achieves this by
 * working with an {@link sgraph.IScenegraphRenderer} interface
 *
 * The scene graph provides functions to store actual geometry (object instances) within itself so
 * that they can be shared within many leaves. Each node of the scene graph keeps a reference to it,
 * enabling any node of the scene graph to directly call its functions.
 *
 * Conversely the scene graph also stores references to all the nodes keyed by their name. This way
 * the scene graph can directly refer to any of its nodes by name instead of traversing the tree
 * every time to find it. This is useful when nodes must be identified and animated in specific
 * ways.
 *
 * @author Amit Shesh
 */
public interface IScenegraph<VertexType extends IVertexData> {

  /**
   * Set the renderer to this scene graph. All specific rendering commands are delegated to this
   * renderer, making the scene graph independent of the rendering implementation
   *
   * @param renderer The {@link IScenegraphRenderer} object that will act as its renderer
   * @throws Exception this is not specific. It exists for future renderers to communicate any
   * exceptions to the scene graph
   */
  void setRenderer(IScenegraphRenderer renderer) throws Exception;

  /**
   * initialize the supplied root to the be the root of this scene graph. This is supposed to
   * overwrite any previous roots
   */
  void makeScenegraph(INode root);

  /**
   * Draw this scene graph, using the stack of modelview matrices provided. The scene graph will use
   * this stack as it navigates its tree.
   */
  void draw(Stack<Matrix4f> modelView);

  /**
   * Enable all the lights. Should be called before draw.
   */
  void lightOn(Stack<Matrix4f> modelView);

  /**
   * Add a polygon mesh that will be used by one or more leaves in this scene graph
   *
   * @param name a unique name by which this mesh may be referred to in future
   * @param obj the {@link util.PolygonMesh} object
   */
  void addPolygonMesh(String name, util.PolygonMesh<VertexType> obj);

  /**
   * Specific scene graph implementations should put code that animates specific nodes in the scene
   * graph, based on a time provided by the caller
   *
   * @param time provides a simple time reference for animation
   */
  void animate(float time);

  /**
   * Adds a node to itself. This should be stored in a suitable manner by an implementation, so that
   * it is possible to look up a specific node by name
   *
   * @param name (hopefully unique) name given to this node
   * @param node the node object
   */
  void addNode(String name, INode node);

  /**
   * Get the root of this scene graph
   *
   * @return the root of this scene graph
   */
  INode getRoot();

  /**
   * Get a mapping of all (name,mesh) pairs that have been added to this scene graph This function
   * is useful in case all meshes of one scene graph have to be added to another in an attempt to
   * merge two scene graphs
   */
  Map<String, util.PolygonMesh<VertexType>> getPolygonMeshes();


  /**
   * Get a mapping of all (name,INode) pairs for all nodes in this scene graph. This function is
   * useful in case all meshes of one scene graph have to be added to another in an attempt to merge
   * two scene graphs
   */
  Map<String, INode> getNodes();

  /**
   * Add a new texture by this name
   */
  void addTexture(String name, String path);

  void dispose();

  void rayTrace(int w, int h, Stack<Matrix4f> modelView, float angleOfView);
}
