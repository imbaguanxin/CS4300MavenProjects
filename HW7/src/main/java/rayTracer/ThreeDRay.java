package rayTracer;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * This class represents a ray in 3d space.
 */
public class ThreeDRay {

  private Vector4f startingPoint;
  private Vector4f direction;

  /**
   * Construct a ray with all variables in float.
   *
   * @param sx x component of the starting point
   * @param sy y component of the starting point
   * @param sz z component of the starting point
   * @param vx x component of the direction
   * @param vy y component of the direction
   * @param vz z component of the direction
   */
  public ThreeDRay(float sx, float sy, float sz, float vx, float vy, float vz) {
    this.startingPoint = new Vector4f(sx, sy, sz, 1);
    this.direction = new Vector4f(vx, vy, vz, 0);
  }

  /**
   * Construct a ray with all variables in Vector4f.
   *
   * @param startingPoint the starting point of ray
   * @param direction the direction of ray
   */
  public ThreeDRay(Vector4f startingPoint, Vector4f direction) {
    this.startingPoint = startingPoint;
    this.direction = direction;
  }

  /**
   * Construct a ray with all variables in Vector3f.
   *
   * @param startingPoint the starting point of ray
   * @param direction the direction of ray
   */
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
