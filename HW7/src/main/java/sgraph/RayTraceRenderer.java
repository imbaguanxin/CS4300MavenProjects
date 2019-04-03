package sgraph;

import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import rayTracer.HitRecord;
import rayTracer.ThreeDRay;
import util.Material;
import util.ObjectInstance;

public class RayTraceRenderer extends LightScenegraphRenderer {

  public RayTraceRenderer() {
    super();
  }

  @Override
  public List<HitRecord> checkHit(String objectName, ThreeDRay ray, Matrix4f modelView,
      Material mat) {
    List<HitRecord> result = new ArrayList<>();
    switch (objectName) {
      case "sphere":
        result.addAll(
            checkHitSphere(
                ray.getStartingPoint(),
                ray.getDirection(),
                new Matrix4f(modelView),
                mat));
        break;
      case "box":
        result.addAll(
            checkHitBox(
                ray.getStartingPoint(),
                ray.getDirection(),
                new Matrix4f(modelView),
                mat));
        break;
      default:
        System.out.println("Not supported shape: " + objectName);
    }
    return result;
  }

  private List<HitRecord> checkHitBox(Vector4f start, Vector4f vector, Matrix4f modelView,
      Material mat) {
    List<HitRecord> result = new ArrayList<>();
    Matrix4f invertedMV = modelView.invert();
    Vector4f s = invertedMV.transform(start);
    Vector4f v = invertedMV.transform(vector);

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
      hIn.setMaterial(mat);

      // hit point goes out the polygon
      //HitRecord hOut = new HitRecord();
      //hOut.setT(tMax);
      // add to list
      result.add(hIn);
      //result.add(hOut);
    }
    return result;
  }

  private List<HitRecord> checkHitSphere(Vector4f start, Vector4f vector, Matrix4f modelView,
      Material mat) {
    List<HitRecord> result = new ArrayList<>();
    Matrix4f invertedMV = modelView.invert();
    Vector4f s = invertedMV.transform(start);
    Vector4f v = invertedMV.transform(vector);

    float a = (float) (Math.pow(v.x, 2) + Math.pow(v.y, 2) + Math.pow(v.z, 2));
    float b = 2f * ((s.x * v.x) + s.y * v.y + s.z * v.z);
    float c = s.x * s.x + s.y * s.y + s.z * s.z - 1;

    float delta = b * b - 4 * a * c;
    if (delta >= 0) {
      float t1 = (-b + (float) Math.sqrt(delta)) / (2 * a);
      float t2 = (-b - (float) Math.sqrt(delta)) / (2 * a);
      float t = Math.min(t1, t2);
      // hit point 1
      HitRecord hIn = new HitRecord();
      hIn.setT(t);
      // compute normal vector in view coordinate
      Vector4f intersection = s.add(v.mul(t));
      Vector4f normal = new Vector4f(intersection).mul(-1);
      normal = modelView.transform(normal);
      hIn.setNormal(normal.x, normal.y, normal.z);
      hIn.setMaterial(mat);
      hIn.setIntersection(intersection);
      // hit point 2
      //HitRecord hOut = new HitRecord();
      //hOut.setT(t2);
      // add to list
      result.add(hIn);
      //result.add(hOut);
    }
    return result;
  }

}
