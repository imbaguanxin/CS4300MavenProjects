package sgraph;

import java.awt.image.BufferedImage;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector4f;
import rayTracer.HitRecord;
import rayTracer.ThreeDRay;
import util.IVertexData;
import util.Light;
import util.Material;
import util.PolygonMesh;

import java.util.*;
import util.TextureImage;

/**
 * A specific implementation of this scene graph. This implementation is still independent of the
 * rendering technology (i.e. OpenGL)
 *
 * @author Amit Shesh
 */
public class Scenegraph<VertexType extends IVertexData> implements IScenegraph<VertexType> {

  /**
   * The root of the scene graph tree
   */
  protected INode root;

  /**
   * A map to store the (name,mesh) pairs. A map is chosen for efficient search
   */
  protected Map<String, util.PolygonMesh<VertexType>> meshes;

  /**
   * A map to store the (name,node) pairs. A map is chosen for efficient search
   */
  protected Map<String, INode> nodes;

  protected Map<String, String> textures;

  /**
   * The associated renderer for this scene graph. This must be set before attempting to render the
   * scene graph
   */
  protected IScenegraphRenderer renderer;


  public Scenegraph() {
    root = null;
    meshes = new HashMap<>();
    nodes = new HashMap<>();
    textures = new HashMap<>();
    this.addTexture("white", "textures/white.png");
  }

  public void dispose() {
    renderer.dispose();
  }

  @Override
  public void rayTrace(int w, int h, Stack<Matrix4f> modelView, float angleOfView) {

    // generate rays
    float distance =
        ((float) Math.max(w, h) / 2) / (float) Math.tan(Math.toRadians(angleOfView / 2));
    rayTracer.ThreeDRay[][] rayArray = new ThreeDRay[h][w];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        float x = -w / 2f + j;
        float y = h / 2f - i;
        float z = -distance;
        rayArray[i][j] = new ThreeDRay(0, 0, 0, x, y, z);
        //System.out.println(x + " " + y + " " + z);
      }
    }

    BufferedImage imageBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    // go through the scenegraph
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        Stack<Matrix4f> mvCopy1 = new Stack<>();
        Stack<Matrix4f> mvCopy2 = new Stack<>();
        for (Matrix4f mv : modelView) {
          mvCopy1.push(new Matrix4f(mv));
          mvCopy2.push(new Matrix4f(mv));
        }
        List<HitRecord> hitRecords = this.root.rayCast(mvCopy1, rayArray[i][j], this.renderer);
        Map<Light, Matrix4f> lights = this.root.getLights(mvCopy2);
        int rgb = 0x000000;
        if (hitRecords.size() > 0) {
          float t = Float.MAX_VALUE;
          HitRecord closestHit = null;
          for (HitRecord record : hitRecords) {
            float newT = record.getT();
            if (newT >= 0 && newT < t) {
              closestHit = record;
              t = newT;
            }
          }
          if (closestHit != null) {
            rgb = shade(closestHit, lights);
          }
        }
        imageBuffer.setRGB(j, i, rgb);
      }
    }


  }

  private int shade(HitRecord hitRecord, Map<Light, Matrix4f> lights) {
    float r, g, b;
    Material material = hitRecord.getMaterial();
    TextureImage textureImage = hitRecord.getTexture();
    Vector2d texCoord = hitRecord.getTextureCoordinate();
    Vector4f materialAmbient = material.getAmbient();
    Vector4f materialDiffuse = material.getDiffuse();
    Vector4f materialSpecular = material.getSpecular();
    Vector4f texRGB = textureImage.getColor((float) texCoord.x, (float) texCoord.y);

    float ambientR = materialAmbient.x;
    float ambientG = materialAmbient.y;
    float ambientB = materialAmbient.z;
    float diffuseR = materialDiffuse.x;
    float diffuseG = materialDiffuse.y;
    float diffuseB = materialDiffuse.z;
    float specularR = materialSpecular.x;
    float specularG = materialSpecular.y;
    float specularB = materialSpecular.z;

    double a = Math.atan2(0, 0);

    r = ((ambientR + diffuseR + specularR) / 3) * 255;
    g = ((ambientG + diffuseG + specularG) / 3) * 255;
    b = ((ambientB + diffuseB + specularB) / 3) * 255;

    r *= texRGB.x;
    g *= texRGB.y;
    b *= texRGB.z;
    return (((Math.round(r) << 2) + Math.round(g)) << 2) + Math.round(b);
  }

  /**
   * Sets the renderer, and then adds all the meshes to the renderer. This function must be called
   * when the scene graph is complete, otherwise not all of its meshes will be known to the
   * renderer
   *
   * @param renderer The {@link IScenegraphRenderer} object that will act as its renderer
   */
  @Override
  public void setRenderer(IScenegraphRenderer renderer) throws Exception {
    this.renderer = renderer;

    //now add all the meshes
    for (String meshName : meshes.keySet()) {
      this.renderer.addMesh(meshName, meshes.get(meshName));
    }

    for (String textureName : textures.keySet()) {
      this.renderer.addTexture(textureName, textures.get(textureName));
    }

  }

  /**
   * Set the root of the scenegraph, and then pass a reference to this scene graph object to all its
   * node. This will enable any node to call functions of its associated scene graph
   */

  @Override
  public void makeScenegraph(INode root) {
    this.root = root;
    this.root.setScenegraph(this);

  }

  /**
   * Draw this scene graph. It delegates this operation to the renderer
   */
  @Override
  public void draw(Stack<Matrix4f> modelView) {
    if ((root != null) && renderer != null) {
      //System.out.println("light on");
      renderer.lightOn(root, modelView);
    }
    if ((root != null) && (renderer != null)) {
      renderer.draw(root, modelView);
    }

  }

  /**
   * Enable all the lights. Should be called before draw.
   */
  @Override
  public void lightOn(Stack<Matrix4f> modelView) {
    if (renderer != null) {
      renderer.lightOn(root, modelView);
    }
  }

  @Override
  public void addPolygonMesh(String name, util.PolygonMesh<VertexType> mesh) {
    meshes.put(name, mesh);
  }

  @Override
  public void animate(float time) {

  }

  @Override
  public void addNode(String name, INode node) {
    nodes.put(name, node);
  }

  @Override
  public INode getRoot() {
    return root;
  }

  @Override
  public Map<String, PolygonMesh<VertexType>> getPolygonMeshes() {
    Map<String, util.PolygonMesh<VertexType>> meshes = new HashMap<String, PolygonMesh<VertexType>>(
        this.meshes);
    return meshes;
  }

  @Override
  public Map<String, INode> getNodes() {
    Map<String, INode> nodes = new TreeMap<String, INode>();
    nodes.putAll(this.nodes);
    return nodes;
  }

  @Override
  public void addTexture(String name, String path) {
    textures.put(name, path);
//    System.out.println(textures);
  }


}
