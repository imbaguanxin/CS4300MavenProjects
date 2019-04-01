package rayTracer;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class ThreeDRay {

  private Vector4f startingPoint;
  private Vector4f direction;

  public ThreeDRay(float sx, float sy, float sz, float vx, float vy, float vz) {
    this.startingPoint = new Vector4f(sx, sy, sz, 1);
    this.direction = new Vector4f(vx, vy, vz, 0);
  }

  public ThreeDRay(Vector4f startingPoint, Vector4f direction) {
    this.startingPoint = startingPoint;
    this.direction = direction;
  }

  public ThreeDRay(Vector3f startingPoint, Vector3f direction) {
    this.startingPoint = new Vector4f(startingPoint.x, startingPoint.y, startingPoint.z, 1);
    this.direction = new Vector4f(direction.x, direction.y, direction.z, 0);
  }

  public Vector4f getStartingPoint() {
    return startingPoint;
  }

  public void setStartingPoint(Vector4f startingPoint) {
    this.startingPoint = new Vector4f(startingPoint);
  }

  public void setStartingPoint(float sx, float sy, float sz) {
    this.startingPoint = new Vector4f(sx, sy, sz, 1);
  }

  public Vector4f getDirection() {
    return direction;
  }

  public void setDirection(Vector4f direction) {
    this.direction = new Vector4f(direction);
  }

  public void setDirection(float vx, float vy, float vz) {
    this.direction = new Vector4f(vx, vy, vz, 0);
  }
}
