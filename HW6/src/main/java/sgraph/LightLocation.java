package sgraph;

public class LightLocation {

  int ambient, diffuse, specular, position, direction, cutOff;

  public LightLocation() {
    ambient = diffuse = specular = position = direction = cutOff = -1;
  }
}
