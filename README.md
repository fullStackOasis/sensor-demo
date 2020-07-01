# Android Accelerometer

App detects the accelerometer sensor and gets `SensorEvent`s from it, prints them out.

Currently, there is no data storage mechanism. The MainActivity class grabs data from the service, which provides them via static variables. Dead simple.

Here's a view of the demo:

![Accelerometer x, y, and z](https://github.com/fullStackOasis/sensor-demo/raw/master/2020-07-01-accelerometer-events.png)
