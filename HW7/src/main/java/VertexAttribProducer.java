import util.VertexProducer;

/**
 * Created by ashesh on 9/3/2016.
 */
public class VertexAttribProducer implements VertexProducer<VertexAttrib> {

  @Override
  public VertexAttrib produce() {
    return new VertexAttrib();
  }
}
