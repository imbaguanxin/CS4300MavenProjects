package sgraph;

import java.util.List;
import org.joml.Matrix4f;
import rayTracer.HitRecord;
import rayTracer.ThreeDRay;
import util.IVertexData;

import java.util.Map;
import java.util.Stack;
import util.Material;

/**
 * This interface provides a general interface for the scene graph to use. Each scene graph is
 * paired with a renderer. Specific implementations of this renderer will encapsulate
 * rendering-specific code (e.g. OpenGL), but this interface itself is independent of specific
 * rendering libraries. This helps in keeping the scene graph independent of specific rendering
 * technologies.
 *
 * @author Amit Shesh
 */
public interface IScenegraphRenderer {

  /**
   * Set a rendering context. Renderers often need a rendering context (e.g. the windowing context,
   * etc.). The parameter is kept very general to support any library. Specific implementations must
   * check if the type matches what they expect.
   *
   * @param obj the rendering context
   * @throws IllegalArgumentException thrown if the type of the rendering context is not as
   * expected.
   */
  void setContext(Object obj) throws IllegalArgumentException;

  /**
   * Initialize the renderer with the shader program. This will also read all relevant shader
   * variables that it must set
   *
   * @param shaderVarsToVertexAttribs a map of shader variables -> vertex attributes in each mesh
   * Every mesh added to this renderer must have the required vertex attributes
   */
  void initShaderProgram(util.ShaderProgram shaderProgram,
      Map<String, String> shaderVarsToVertexAttribs);

  /**
   * Get the location of a particular shader variable. Renderers for individual meshes will need
   * this to provide mesh-specific properties like material to the shaders. The intention is that
   * the renderer stores only what is required to render it, not necessarily the entire mesh
   * itself.
   *
   * @return an integer handle for the appropriate variable if it exists, -1 otherwise
   */
  int getShaderLocation(String name);

  /**
   * Add a mesh to be rendered in the future.
   *
   * @param name the name by which this mesh is referred to by the scene graph
   * @param mesh the {@link util.PolygonMesh} object that represents this mesh
   * @throws Exception general mechanism to let the scene graph know of any problems
   */
  <K extends IVertexData> void addMesh(String name, util.PolygonMesh<K> mesh) throws Exception;

  /**
   * Draw the scene graph rooted at supplied node using the supplied modelview stack. This is
   * usually called by the scene graph
   */
  void draw(INode root, Stack<Matrix4f> modelView);

  /**
   * This is a method should be called before draw to enable the light in the scenegraph
   *
   * @param root The passed in root directs the renderer to find all lights
   * @param modelView Similar to draw, this is a stack of modelView.
   */
  void lightOn(INode root, Stack<Matrix4f> modelView);

  /**
   * Draw a specific mesh. This is called from a leaf node of the associated scene graph
   */
  void drawMesh(String name, util.Material material, String textureName,
      final Matrix4f transformation);

  /**
   * Add a new texture with the given name and the path to the actual image file Implementation of
   * this function is dependent on the implementation of the renderer
   */
  void addTexture(String name, String path);

  void dispose();

  List<HitRecord> checkHit(String objectName, ThreeDRay ray, Matrix4f modelView, Material mat,
      String textureName);
}
