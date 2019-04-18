package sgraph;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
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
   * The index of the image that is to be produced.
   */
  private static int imgIndex = 0;

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

  protected final int MAX_REFLECTION_COUNT = 5;
  protected final int MAX_REFRACTION_COUNT = 1;


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

  /**
   * Start ray trace this scene graph and write an image.
   *
   * @param w the width of the image
   * @param h the height of the image
   * @param modelView the stack of modelView storing a transformation from world to camera
   * @param angleOfView angle of view from bottom to top
   */
  @Override
  public void rayTrace(int w, int h, Stack<Matrix4f> modelView, float angleOfView) {

    float distance =
        (h * 0.5f) / (float) Math.tan(Math.toRadians(angleOfView / 2));
    rayTracer.ThreeDRay[][] rayArray = new ThreeDRay[h][w];

    BufferedImage imageBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        // generate rays
        float x = -w / 2f + j;
        float y = h / 2f - i;
        float z = -distance;
        rayArray[i][j] = new ThreeDRay(0, 0, 0, x, y, z);

        // copy modelView
        Stack<Matrix4f> mvCopy1 = copyMV(modelView);
        Stack<Matrix4f> mvCopy2 = copyMV(modelView);
        // generate hit records
        List<HitRecord> hitRecords = this.root.rayCast(mvCopy1, rayArray[i][j], this.renderer);
        // gather lights from nodes. the according matrix4f is light to view
        Map<Light, Matrix4f> lights = this.root.getLights(mvCopy2);

        // produce color for this pixel
        Vector3f rgb = this.getRGB(hitRecords, lights, modelView, new Vector4f(0, 0, 0, 1),
            MAX_REFLECTION_COUNT);
        imageBuffer.setRGB(j, i, new Color(rgb.x, rgb.y, rgb.z).getRGB());
      }
    }
    // Write image produced to file.
    try {
      imgIndex++;
      String filename = String.format("image%03d.png", imgIndex);
      File output = new File(filename);
      ImageIO.write(imageBuffer, "png", output);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Vector3f refraction(HitRecord hitRecord, Map<Light, Matrix4f> lights,
      Stack<Matrix4f> modelView, int bound, Vector4f fromPoint) {
    if (bound <= 0) {
      Vector3f res = shade(hitRecord, lights, modelView);
      //System.out.println("bound == 0" + res.toString());
      return res;
    }
    Stack<Matrix4f> mvCopy = copyMV(modelView);

    // Construct the refraction ray
    Vector4f intersection = hitRecord.getIntersection();
    Vector4f normal = hitRecord.getNormal();
    if (hitRecord.getFlipNormal()) {
      normal = normal.mul(-1);
    }

    Vector4f inDir = new Vector4f(intersection).sub(fromPoint);
    inDir = inDir.normalize();
    float cosIn = new Vector4f(inDir).dot(normal) * -1f;
    float sinIn = (float) Math.sqrt(1 - cosIn * cosIn);
    float sinOut = sinIn * hitRecord.getFromRefraction() / hitRecord.getToRefraction();
    if (sinOut > 1f) {
      return reflect(hitRecord, lights, modelView, bound, fromPoint);
    } else {
      float cosOut = (float) Math.sqrt(1 - sinOut * sinOut);
      Vector4f refraction = null;
      if (sinIn < 0.001f && sinIn > -0.001f) {
        refraction = new Vector4f(normal).mul(-1);
      } else {
        Vector4f b = new Vector4f(normal).mul(-1).mul(cosOut);
        Vector4f a = new Vector4f(inDir).add(new Vector4f(normal).mul(cosIn)).mul(sinOut / sinIn);
        refraction = a.add(b);
      }
      // refraction
      intersection = intersection.add(new Vector4f(normal).mul(-.001f));
      ThreeDRay refractionRay = new ThreeDRay(intersection.x, intersection.y, intersection.z,
          refraction.x, refraction.y, refraction.z);
      List<HitRecord> refractionRecords = this.root.rayCast(mvCopy, refractionRay, this.renderer);
      Vector3f rgb = getRGB(refractionRecords, lights, modelView, intersection,
          bound - 1);
      return rgb;
    }
  }

  private Vector3f reflect(HitRecord hitRecord, Map<Light, Matrix4f> lights,
      Stack<Matrix4f> modelView, int bound, Vector4f fromPoint) {
    if (bound <= 0) {
      return new Vector3f(0, 0, 0);
    }

    Stack<Matrix4f> mvCopy = copyMV(modelView);

    // Construct the reflection ray
    Vector4f intersection = hitRecord.getIntersection();
    Vector4f normal = hitRecord.getNormal();
    Vector4f inDir = new Vector4f(intersection).sub(fromPoint);
    Vector4f reflectDir =
        new Vector4f(new Vector3f(inDir.x, inDir.y, inDir.z)
            .normalize()
            .reflect(new Vector3f(normal.x, normal.y, normal.z).normalize()), 0);
    ThreeDRay reflectRay = new ThreeDRay(intersection.x, intersection.y, intersection.z,
        reflectDir.x, reflectDir.y, reflectDir.z);
    List<HitRecord> reflectRecords = this.root.rayCast(mvCopy, reflectRay, this.renderer);

    Vector3f rgb = getRGB(reflectRecords, lights, modelView, hitRecord.getIntersection(),
        bound - 1);

    return rgb;
  }

  /**
   * This method acts as a combine of vertex and fragment shader for a pixel.
   *
   * @param hitRecord the closest hit on the ray
   * @param lights all lights in this scene
   * @return the color of this pixel
   */
  private Vector3f shade(HitRecord hitRecord, Map<Light, Matrix4f> lights,
      Stack<Matrix4f> modelView) {
    // data type adapting
    Material material = hitRecord.getMaterial();
    TextureImage textureImage = hitRecord.getTexture();
    Vector4f position = hitRecord.getIntersection();
    Vector4f normal = hitRecord.getNormal();
    Vector2f texCoord = hitRecord.getTextureCoordinate();
    Vector3f materialAmbient = new Vector3f(
        material.getAmbient().x,
        material.getAmbient().y,
        material.getAmbient().z);
    Vector3f materialDiffuse = new Vector3f(
        material.getDiffuse().x,
        material.getDiffuse().y,
        material.getDiffuse().z);
    Vector3f materialSpecular = new Vector3f(
        material.getSpecular().x,
        material.getSpecular().y,
        material.getSpecular().z);
    float materialShininess = material.getShininess();
    //float BRIGHTNESS = 0.4f;

    // pass correct texture coordinates to fragment shader
    Matrix4f textureTrans = new Matrix4f().identity();
    if (textureImage.getTexture().getMustFlipVertically()) {
      textureTrans = new Matrix4f().translate(0, 1, 0).scale(1, -1, 1);
    }
    // vertex shader jobs
    Vector4f newTexCoord = textureTrans.transform(new Vector4f(texCoord.x, texCoord.y, 0, 1));

    // fragment shader jobs
    Vector3f lightVec, viewVec, reflectVec, normalLightDirect;
    Vector3f normalView;
    Vector3f ambient, diffuse, specular;
    float nDotL, rDotV, dDotMinusL;
    //Vector4f color = new Vector4f(0,0,0,1);
    Vector4f color = new Vector4f(materialAmbient.x, materialAmbient.y, materialAmbient.z, 1);

    for (Light light : lights.keySet()) {
      float lightIndex = canSeeLight(hitRecord, light, new Matrix4f(lights.get(light)), modelView);
//      System.out.println("light index:" + lightIndex);
      if (lightIndex > 0) {
        Vector4f lightPosition = lights.get(light).transform(new Vector4f(light.getPosition()));
        Vector4f lightDirection = lights.get(light)
            .transform(new Vector4f(light.getSpotDirection()));

        if (light.getPosition().w != 0) {
          lightVec = new Vector3f(
              lightPosition.x - position.x,
              lightPosition.y - position.y,
              lightPosition.z - position.z)
              .normalize();
          normalLightDirect = new Vector3f(
              lightDirection.x,
              lightDirection.y,
              lightDirection.z)
              .normalize();
        } else {
          lightVec = new Vector3f(
              -lightPosition.x,
              -lightPosition.y,
              -lightPosition.z)
              .normalize();
          normalLightDirect = new Vector3f(
              lightPosition.x,
              lightPosition.y,
              lightPosition.z)
              .normalize();
        }

        Vector3f tNormal = new Vector3f(normal.x, normal.y, normal.z);
        normalView = new Vector3f(tNormal).normalize();
        nDotL = normalView.dot(lightVec);

        viewVec = new Vector3f(-position.x, -position.y, -position.z);
        viewVec = viewVec.normalize();

        reflectVec = new Vector3f(-lightVec.x, -lightVec.y, -lightVec.z).reflect(normalView);
        reflectVec = reflectVec.normalize();

        rDotV = Math.max(reflectVec.dot(viewVec), 0);

        ambient = new Vector3f(materialAmbient).mul(light.getAmbient());
        diffuse = new Vector3f(materialDiffuse).mul(light.getDiffuse()).mul(Math.max(nDotL, 0));
        if (nDotL > 0) {
          specular = new Vector3f(materialSpecular).mul(light.getSpecular())
              .mul((float) Math.pow(rDotV, materialShininess));
        } else {
          specular = new Vector3f(0, 0, 0);
        }

        dDotMinusL = new Vector3f(
            -normalLightDirect.x,
            -normalLightDirect.y,
            -normalLightDirect.z)
            .dot(lightVec);

        if (dDotMinusL > light.getSpotCutoff()) {
          Vector3f colorTemp = new Vector3f(ambient).add(diffuse).add(specular).mul(lightIndex);
          color = color.add(new Vector4f(colorTemp.x, colorTemp.y, colorTemp.z, 1));
        }
      }
    }

    // sample the texture image
    Vector4f texRGB = textureImage.getColor(newTexCoord.x, newTexCoord.y);

    color = color.mul(texRGB);

    return new Vector3f(Math.min(color.x, 1f), Math.min(color.y, 1f), Math.min(color.z, 1f));
  }


  private Vector3f getRGB(List<HitRecord> records, Map<Light, Matrix4f> lights,
      Stack<Matrix4f> modelView, Vector4f fromPoint, int bound) {
    Stack<Matrix4f> mvCopy = copyMV(modelView);
    Vector3f rgb = null;
    if (records.size() > 0) {
      HitRecord closestHit = null;
      float minT = Float.MAX_VALUE;
      for (HitRecord hit : records) {
        if (hit.getT() > 0.01 && hit.getT() < minT) {
          closestHit = hit;
          minT = hit.getT();
        }
      }
      if (closestHit != null) {
        Material mat = closestHit.getMaterial();
        rgb = shade(closestHit, lights, mvCopy).mul(mat.getAbsorption());
        if (mat.getReflection() > 0) {
          Vector3f reflectRGB = reflect(closestHit, lights, modelView, bound,
              fromPoint).mul(mat.getReflection());
          rgb = rgb.add(reflectRGB);
        }
        float refract = 1 - mat.getAbsorption() - mat.getReflection();
        if (refract > 0) {
          Vector3f refractionRGB = refraction(closestHit, lights, modelView, bound,
              fromPoint).mul(refract);
          rgb = rgb.add(refractionRGB);
        }
      }
    }
    if (rgb == null) {
      rgb = new Vector3f(0, 0, 0);
    }

    rgb = new Vector3f(Math.min(1f, rgb.x), Math.min(1f, rgb.y), Math.min(1f, rgb.z));
    return rgb;
  }

  private float canSeeLight(HitRecord hitRecord, Light light, Matrix4f LightModelView,
      Stack<Matrix4f> modelView) {
    // copy model view
    Stack<Matrix4f> mvCopy = new Stack<>();
    for (Matrix4f mv : modelView) {
      mvCopy.push(mv);
    }
    // find the starting point of light in view
    Vector4f lightPosition = LightModelView.transform(new Vector4f(light.getPosition()));
    // find the hit position in view
    Vector4f hitPosition = new Vector4f(hitRecord.getIntersection());
    ThreeDRay hitToLightRay = null;
    if (lightPosition.w != 0) { // A spot light
      Vector3f hitToLightDir = new Vector3f(
          lightPosition.x - hitPosition.x,
          lightPosition.y - hitPosition.y,
          lightPosition.z - hitPosition.z);
      // find out the the direction from hit point to light origin, normalized
      Vector3f normalHitToLightDir = new Vector3f(hitToLightDir).normalize();
      float hitToLightDotNormal = new Vector4f(hitRecord.getNormal())
          .dot(new Vector4f(normalHitToLightDir, 0));
      // light is parallel to the given surface.
      if (hitToLightDotNormal < 0.001f && hitToLightDotNormal > -0.001f) {
        return 0;
      } else {
        Vector3f position = new Vector3f(hitPosition.x, hitPosition.y, hitPosition.z)
            .add(normalHitToLightDir.mul(0.005f));
        hitToLightRay = new ThreeDRay(position, hitToLightDir);
        List<HitRecord> records = this.root.rayCast(mvCopy, hitToLightRay, this.renderer);
        float transparency = 1;
        for (HitRecord hit : records) {
          if (hit.getT() < 0.99 && hit.getT() > 0.001f) {
            Material mat = hit.getMaterial();
            float refraction = 1 - mat.getAbsorption() - mat.getReflection();
            transparency = transparency * refraction;
          }
        }
        return transparency;
      }
    } else {
      Vector3f hitToLightDir = new Vector3f(
          -lightPosition.x,
          -lightPosition.y,
          -lightPosition.z)
          .normalize();
      Vector3f position = new Vector3f(hitPosition.x, hitPosition.y, hitPosition.z)
          .add(new Vector3f(hitToLightDir).mul(0.005f));
      float hitToLightDotNormal = new Vector4f(hitRecord.getNormal())
          .dot(new Vector4f(hitToLightDir, 0));
      // light is parallel to the given surface.
      if (hitToLightDotNormal < 0.001f && hitToLightDotNormal > -0.001f) {
        return 0;
      } else {
        hitToLightRay = new ThreeDRay(position, hitToLightDir);
        List<HitRecord> records = this.root.rayCast(mvCopy, hitToLightRay, this.renderer);
        float transparency = 1;
        for (HitRecord hit : records) {
          if (hit.getT() > 0.01f) {
            Material mat = hit.getMaterial();
            float refraction = 1 - mat.getAbsorption() - mat.getReflection();
            transparency = transparency * refraction;
          }
        }
        return transparency;
      }
    }
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
    if ((root != null) && (renderer != null)) {
      renderer.lightOn(root, modelView);
      renderer.draw(root, modelView);
    }
  }

  /**
   * Enable all the lights. Should be called before draw.
   */
  @Override
  public void lightOn(Stack<Matrix4f> modelView) {
    if (root != null && renderer != null) {
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

  private Stack<Matrix4f> copyMV(Stack<Matrix4f> modelView) {
    Stack<Matrix4f> copy = new Stack<>();
    for (Matrix4f mv : modelView) {
      copy.add(mv);
    }
    return copy;
  }


}
