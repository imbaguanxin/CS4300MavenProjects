package sgraph;

public class LightLocation {

  int ambient, diffuse, specular, position, direction, cutOff;

  public LightLocation() {
    ambient = diffuse = specular = position = direction = cutOff = -1;
  }


  public int getAmbient() {
    return ambient;
  }

  public int getDiffuse() {
    return diffuse;
  }

  public int getSpecular() {
    return specular;
  }

  public int getPosition() {
    return position;
  }

  public int getDirection() {
    return direction;
  }

  public int getCutOff() {
    return cutOff;
  }

  public void setAmbient(int ambient) {
    this.ambient = ambient;
  }

  public void setDiffuse(int diffuse) {
    this.diffuse = diffuse;
  }

  public void setSpecular(int specular) {
    this.specular = specular;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public void setDirection(int direction) {
    this.direction = direction;
  }

  public void setCutOff(int cutOff) {
    this.cutOff = cutOff;
  }
}
