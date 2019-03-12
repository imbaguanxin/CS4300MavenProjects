# CS4300 HW5

## Set Config Files to Run the Code

  All the config files are under `src/main/resources/configs`

  For example, just copy `src/main/resources/configs/old_hall_drone.config` to configuration and the code is going to run.

  `old_hall_drone.config` is displaying the drone. The main window is showing the global view and the small top right window is showing the drone camera's view.

  Similarily, other 3 files work as well, including `camera,config`, `tower_global.config`, `tower_drone.config` and `YMCA_global.config`.

## Design of animation

We created a new class to enables animation that builds on the given Scenegraph. The new class is called `RotateScenegraph`. We adopted a composite pattern. It takes in a Scenegraph, a rotate axis, a translate vector and an angular speed. The passed in Scenegraph is stored as a field called `originalScenegraph`; the translate vector are stored as a matrix called `translateMatrix`; the passed in rotate axis is called `axis` to guid the production of a rotate matrix.
   
### changes in the new design

Only the animate method and draw method are newly designed. All other methods are delegated by the taken in scenegraph. The animate method takes in a time and calculates how the Scenegraph is rotated. The draw method push the rotate and translate information to the passed in modelView and then draw the `originalScenegraph` accordingly. 

### Reason for our design

We gave up modifying the `Scenegraph` but created a new class because we don't want to mess up with old codes. In addition, we just need to add a new class If I want to animate in different ways in the future and won't bother thinking about compatibility with the old animation. 
   
### Limitations

Composition requires to write more code. Also, we need to keep an eye on changes on `Scenegraph` class and `IScenegraph` interface in the future. We need to add extra code to support new features of `Scenegraph` for each animation. It is painful if we have a large number of distinct "Animatable Scenegraphs".

### How to change the animation

I personally recommend to design a new class that also follow the composite pattern. To modify our code, here are some steps:

  1. Pass in required information in construction
     
     Give required information such as rotate angle, rotate axis, translate parameters or scale parameters.
     
  2. modify animate method
  
     The passed in float in animate method is just a time reference. User have to figure out how the object should be transformed given time. Ideally, user should store the change in matrix form.
     
  3. modify draw method
  
     `View` class is going to call draw after animate which pass in a time. In draw, user should apply the animation by representing it as a matrix. Push the change to the modelView and then call draw on the originalScenegraph. Look like:
     ```java
     public void draw(Stack<Matrix4f> modelView){
         modelView.push(new Matrix4f(modelView.peek()));
         // TransformAccodingTime is how should a object transformed according to time.
         modelView.peek().mul(TransformAccordingTime);
         originalScenegraph.draw(modelView);
         modelView.pop();
     }
     ```

## Notes

   When turing right and left, the drone is going to turn with the camera. When the camera is not in the horizontal plane, the turning angular speed of the drone may vary according to the posture of the camera. To be precise, the angular speed of drone is the angular speed of the camera's front direction's projection on to the horizontal plane. We do such calculate to ensure that the direction of the camera and the direction of the drone are on the same vertical plane. So visually, they point to the same direction.