# Android Accelerometer

App detects the accelerometer sensor and gets `SensorEvent`s from it, prints them out.

Currently, there is no data storage mechanism. The MainActivity class grabs data from the service, which provides them via static variables. Dead simple.
