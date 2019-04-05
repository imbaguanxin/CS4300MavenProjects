package sgraph;

import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import rayTracer.HitRecord;
import rayTracer.ThreeDRay;
import util.Material;
import util.ObjectInstance;
import util.TextureImage;

public class RayTraceRenderer extends LightScenegraphRenderer {

  public RayTraceRenderer() {
    super();
  }

  @Override
  public List<HitRecord> checkHit(String objectName, ThreeDRay ray, Matrix4f modelView,
      Material mat, String textureName) {
    List<HitRecord> result = new ArrayList<>();
    switch (objectName) {
      case "sphere":
        result.addAll(
            checkHitSphere(
                ray.getStartingPoint(),
                ray.getDirection(),
                new Matrix4f(modelView),
                mat,
                textureName));
        break;
      case "box":
        result.addAll(
            checkHitBox(
                ray.getStartingPoint(),
                ray.getDirection(),
                new Matrix4f(modelView),
                mat,
                textureName));
        break;
      default:
        System.out.println("Not supported shape: " + objectName);
    }
    return result;
  }

  private List<HitRecord> checkHitBox(Vector4f start, Vector4f vector, Matrix4f modelView,
      Material mat, String textureName) {

    List<HitRecord> result = new ArrayList<>();
    Matrix4f invertedMV = new Matrix4f();
    modelView.invert(invertedMV);
    Vector4f s = new Vector4f();
    Vector4f v = new Vector4f();
    invertedMV.transform(start, s);
    invertedMV.transform(vector, v);

    float txMin = Math.min((-0.5f - s.x) / v.x, (0.5f - s.x) / v.x);
    float txMax = Math.max((-0.5f - s.x) / v.x, (0.5f - s.x) / v.x);
    float tyMin = Math.min((-0.5f - s.y) / v.y, (0.5f - s.y) / v.y);
    float tyMax = Math.max((-0.5f - s.y) / v.y, (0.5f - s.y) / v.y);
    float tzMin = Math.min((-0.5f - s.z) / v.z, (0.5f - s.z) / v.z);
    float tzMax = Math.max((-0.5f - s.z) / v.z, (0.5f - s.z) / v.z);

    float tMin = Math.max(Math.max(txMin, tyMin), tzMin);
    float tMax = Math.min(Math.min(txMax, tyMax), tzMax);
    if (tMin <= tMax) {
      // hit point goes in the polygon
      HitRecord hIn = new HitRecord();
      hIn.setT(tMin);

      // set material
      hIn.setMaterial(mat);

      // intersection in View
      Vector4f intersectionInView = new Vector4f(start).add(new Vector4f(vector).mul(tMin));
      hIn.setIntersection(intersectionInView);

      // calculate normal vector
      Vector4f normal = new Vector4f(0, 0, 0, 0);
      // find intersection in obj coordinate system
      Vector4f intersection = new Vector4f(s).add(new Vector4f(v).mul(tMin));
      // find intersection in obj coordinate system
      if (intersection.x == .5f) {
        normal.x = 1f;
      } else if (intersection.x == -.5f) {
        normal.x = -1f;
      }
      if (intersection.y == .5f) {
        normal.y = 1f;
      } else if (intersection.y == -.5f) {
        normal.y = -1f;
      }
      if (intersection.z == .5f) {
        normal.z = 1f;
      } else if (intersection.z == -.5f) {
        normal.z = -1f;
      }
      Matrix4f invTranspose = new Matrix4f(modelView).transpose().invert();
      invTranspose.transform(normal);
      hIn.setNormal(normal.x, normal.y, normal.z);

      // set texture
      TextureImage image = this.textures.get(textureName);
      if (!textureName.equals("") && image != null) {
        hIn.setTextureImage(image);
        float textureX = 0, textureY = 0;
        if (intersection.x == .5f) {
          textureX = .5f + .25f * (intersection.z + .5f);
          textureY = .5f - .25f * (intersection.y + .5f);
        } else if (intersection.x == -.5f) {
          textureX = .25f - .25f * (intersection.z + .5f);
          textureY = .5f - .25f * (intersection.y + .5f);
        } else if (intersection.y == .5f) {
          textureX = .25f + .25f * (intersection.x + .5f);
          textureY = .25f - .25f * (intersection.z + .5f);
        } else if (intersection.y == -.5f) {
          textureX = .25f + .25f * (intersection.x + .5f);
          textureY = .5f + .25f * (intersection.z + .5f);
        } else if (intersection.z == .5f) {
          textureX = 1f - .25f * (intersection.x + .5f);
          textureY = .5f - .25f * (intersection.y + .5f);
        } else if (intersection.z == -.5f) {
          textureX = .25f + .25f * (intersection.x + .5f);
          textureY = .5f - .25f * (intersection.y + .5f);
        }
//        if (Math.abs(intersection.x) == .5) {
//          x = intersection.y + .5f;
//          y = intersection.z + .5f;
//        } else if (Math.abs(intersection.y) == .5) {
//          x = intersection.x + .5f;
//          y = intersection.z + .5f;
//        } else if (Math.abs(intersection.z) == .5) {
//          x = intersection.x + .5f;
//          y = intersection.y + .5f;
//        }
        hIn.setTextureCoordinate(textureX, textureY);
      }
//      System.out.println("box");
//      System.out.println("point:" + hIn.getIntersection());
//      System.out.println("normal: " + hIn.getNormal());
      // add the hit to hit records
      result.add(hIn);

    }
    return result;
  }

  private List<HitRecord> checkHitSphere(Vector4f start, Vector4f vector, Matrix4f modelView,
      Material mat, String textureName) {
    List<HitRecord> result = new ArrayList<>();
    Matrix4f invertedMV = new Matrix4f();
    modelView.invert(invertedMV);
    Vector4f s = new Vector4f();
    Vector4f v = new Vector4f();
    invertedMV.transform(start, s);
    invertedMV.transform(vector, v);

    float a = v.x * v.x + v.y * v.y + v.z * v.z;
    float b = 2f * (s.x * v.x + s.y * v.y + s.z * v.z);
    float c = s.x * s.x + s.y * s.y + s.z * s.z - 1;

    float delta = b * b - 4 * a * c;
    if (delta >= 0) {
      float t1 = (-b + (float) Math.sqrt(delta)) / (2 * a);
      float t2 = (-b - (float) Math.sqrt(delta)) / (2 * a);
      float t = Math.min(t1, t2);
      // hit point
      HitRecord hIn = new HitRecord();
      hIn.setT(t);

      // compute normal vector in view coordinate
      // intersection in obj coordinate system
      Vector4f intersection = new Vector4f(s).add(new Vector4f(v).mul(t));
      // normal vector in obj coordinate system
      Vector4f normal = new Vector4f(intersection);
      Matrix4f invTranspose = new Matrix4f(modelView).transpose().invert();
      invTranspose.transform(normal);
      hIn.setNormal(normal.x, normal.y, normal.z);

      // set material
      hIn.setMaterial(mat);

      // set intersection in View
      Vector4f intersectionInView = new Vector4f(start).add(new Vector4f(vector).mul(t));
      hIn.setIntersection(intersectionInView);

      // set texture
      TextureImage image = this.textures.get(textureName);
      if (!textureName.equals("") && image != null) {
        hIn.setTextureImage(image);
        float phi = (float) Math.asin(intersection.y);
        float theta = (float) Math.atan2(intersection.z, intersection.x);
        float imageT = phi / (float) Math.PI + .5f;
        float imageS = theta / (2 * (float) Math.PI) + .5f;
//        System.out.println("t: " + imageT + "s: " + imageS);
        hIn.setTextureCoordinate(imageT, imageS);
      }
//      Vector4f getNorm = hIn.getNormal();
//      if (Math.abs(getNorm.x) < 0.1 && Math.abs(getNorm.y) < 0.1) {
//        System.out.println("sphere");
//        System.out.println("point:" + hIn.getIntersection());
//        System.out.println("normal: " + hIn.getNormal());
//      }

      // add to list
      result.add(hIn);
    }
    return result;
  }

}
