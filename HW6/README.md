# CS4300 HW5

# USER MANUAL

## Set Config Files to Run the Code

  All the config files are under `src/main/resources/configs`

  For example, just copy `src/main/resources/configs/old_hall_drone.config` to configuration and the code is going to run.

  `old_hall_drone.config` is displaying the drone. The main window is showing the global view and the small top right window is showing the drone camera's view.

  Similarly, other 3 files work as well, including `camera.config`, `tower_global.config`, `tower_drone.config` and `YMCA_global.config`.

## Camera Operations

  There are two different mode to observe in this virtual world. You can switch between the two using `Space` key on your keyboard.

  1. Global View

  In the global view, you can observe this virtual world from a constant distance, which is defined by the config file you passed in. In this view you can use your mouse to drag the world like a trackball.

  2. Drone View

  In the drone view, you can view this virtual world through a camera mounted on an unmanned drone flying around. Press `up`, `down`, `left`, `right` to move the drone. Press `w`, `a`, `s`, `d` to turn the direction of the camera. You can also use `f`, `c` to make the camera to slope to the left or right. Press `+`, `-` to zoom in or out, but notice there is a limitation on zooming too far or too near.

# Design

  1. Configuration file

  A template for a valid config file should looks like this:

  ```
  path [path to the xml scene graph file]
  mode [GLOBAL/MOVING]
  fix-position [x] [y] [z]
  fix-center [x] [y] [z]
  move-position [x] [y] [z]
  move-center [x] [y] [z]
  ```
  The fix/move-position specifies the position of the camera in global/drone view; and the fix/move-center specifies the point the camera is facing in global/drone view. The up direction of the camera is set to the positive-y direction by default.

  2. Daylight in the scene

  Besides all the light comes with the Scene graph, our scene have a white daylight shining from the above, i.e. shining to the negative-y direction.
