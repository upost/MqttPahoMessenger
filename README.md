# MqttPahoMessenger
Simple Android chat/messenger app skeleton based on Paho and MQTT.

Note that the Eclipse sandbox MQTT broker configured in this sample might become unavailable at any time.
You can replace it with your own broker's address or another one which is freely available like test.mosquitto.org.

Use Android Studio 4.1 to build this project.

Notice: The Paho client (https://github.com/upost/MqttPahoMessenger) is not maintained since june 2020 as 
of writing this (august 2020) and it still requires the old support-v4
library which is deprecated since a long time. I'd currently not start new projects based on Paho client due 
to lack of developer support. Sadly, there's no alternative I know of (you might want to try https://github.com/hivemq/hivemq-mqtt-client 
but this is not Android optimized and I didn't get it running), so you'll propably move to Firebase Cloud Messaging.