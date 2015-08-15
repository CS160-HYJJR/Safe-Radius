## Safe Radius
Final Project of Group HYJJR (Group I) in CS160 Summer 2015 UC Berkeley     
Course website: [Link](http://cs160.valkyriesavage.com/schedule.html)      
Author: Harry He, Riva Madan, Yue Cao, Judy Lai, Donghao Su      
Please read this before running the program.    

#I. Introduction  
Safe Radius aims to help people alleviate stress when traveling with young children. 
After placing your smartwatch on your child's wrist, you can be notified when they leave a preset safe radius, communicate with them through messages, and find their location on a map. You can also customize the homepage of the app by adding a picture of yourself that will show up on the child's watch notification when they receive a message or set a background photo for personalization purposes. 

#II. Implementation Note
Safe Radius intends to work on a watch with builtin GPS hardware. However, because the Moto360, which is used in CS160, does not have GPS capabilities, we give the watch the GPS capabilities by pairing it with a hidden phone. We assume the hidden phone always stay with the watch and the watch and phone stay with the child. We need another unhidden phone for the parent to do the actual interaction. Hidden phone and watch should be paired by bluetooth. Hidden phone acts as a bridge between unhidden phone and watch. All information exchanged between unhidden phone and watch comes through hidden phone. Two phones are not paired in any way. They simply broadcast to all devices running with same code. As a result, if you run the program in many devices, they will interfere with each other. Therefore, don’t  run the app on multiple devices except the hidden and unhidden phones you choose.
In conclusion, in order to run this app, we need one Android Watch and Two phones: one hidden and one unhidden.

#III. System Requirement
1. Unhidden phone: Requires API 21 (Android 5.0). The interface may be rendered incorrectly in lower version. The app cannot be installed on phone with system version lower than API 18 (Android 4.3). The phone must have builtin GPS hardware in the unhidden phone, newest Google Play Service installed and have good connection to internet.

2. Hidden phone: Requires API 18 (Android 4.3). The interface may be rendered incorrectly in the hidden phone, but it should not matter. The hidden phone should have GPS hardware, have newest Google Play Service installed and have good connection to internet. The hidden phone must also have bluetooth in order to connect to the watch.

3. Watch: Requires API 22 (Android 5.1). Tested on Moto360. The watch must have bluetooth in order to connect to hidden phone. Microphone is required in order to use voice message.  Speaker is not required.

#IV. Installation Guide
1. Unhidden phone: Enable GPS and disable firewall. Make sure the phone is connected to the internet and the network speed is good. Check Google Map to make sure there is  good GPS signal. You can check the measurement accuracy by the blue circle in Google Map. If its radius size is larger than 30feet, the app will run very inaccurately. Install the app by [app.apk](https://github.com/CS160-HYJJR/Safe-Radius/raw/master/app.apk)

2. hidden phone: Do the same thing as unhidden phone. Make sure the phone is connected to the internet and the network speed is good. Then pair it with the watch. Install the app on the hidden phone and watch by [app.apk](https://github.com/CS160-HYJJR/Safe-Radius/raw/master/app.apk)

3. watch: make sure the watch has paired to the hidden phone.

Open the app in all three devices. Let the hidden phone and watch stay with the child and let the unhidden phone stay with the parent. Although the hidden phone does not involve in the interaction between unhidden phone and watch, make sure the app keeps running in the hidden phone. Don’t run any other instances of app in other devices.

If app.apk does not work, you can also try [mobile-debug.apk](https://github.com/CS160-HYJJR/Safe-Radius/raw/master/mobile-debug.apk) and [wear-debug.apk](https://github.com/CS160-HYJJR/Safe-Radius/raw/master/wear-debug.apk)

#V. Functionalities
1. Hidden phone: No function. It just exists to give watch GPS capabilities.
2. Unhidden phone:
  1. The status bar at the bottom shows if the phone has connected to the watch by detecting whether it receives any message from watch in recent 6s.
  2. If the phone has connected to the watch, the parent can use the top spinner in main tab to select message. The parent can add new message or delete message as he wishes. The parent can press send button to send message to the child and then the toast message at the bottom show if it is success or not. The message configuration stays after the app closes. The child will see the message on the phone as a notification.
  3. The parent can click on the top left corner to take a picture to change the background in the main tab.
  4. The parent can click on the “add a pic” circle button to take a picture of themselves. If the connection between phone and watch is good, the watch will receive the picture and the watch app will use that picture as the background of notification.
  5. The parent can change, add, delete the safe radius in the radius spinner. The configuration remains after the app closes. If the app detects the distance between child and parent is larger than safe radius, the parent will get an alert.
  6. The parent can see the location of himself and his child by switching to map tab. The parent can press the button on the top right to center the screen on himself. The location  of child is shown as a red dot. If the child is out of the screen, an red arrow will show and point to the location of child.
  7. The parent will get an alert, showing child’s message, if the child sends a message to them.
  8. The parent can turn off the app by pressing the button on the top right corner. This button turn off the usage of GPS and all data transfer.
  9. The parent can get alert even if the app is running in background.

3. Watch
  1. Child can get a notification when he receives a message from parent. The background of the notification can be set by the parent. The child can swipe right on the notification and press “Got it” button to confirm their parent that they have read the message.
  2. In the main screen, the child can press the envelope button to quick send message “Come and find Me” to his parent.
  3. The child can press the microphone button to send voice message to parent. The voice is converted to text automatically and then sent to parent.

#VI. Known Issues.
1. We have made a lot of efforts in order to make the app work on the watch without GPS hardware. The complex connection between 3 devices cause some problem. In order to send a message from watch to unhidden phone, the watch needs to send it to hidden phone first and then hidden phone sends to to a cloud server and then the cloud server broadcasts it into the unhidden phone. It requires good internet connection to work and all data transfers have some delay. This is much slower compared to transferring data between a paired phone and watch directly.

2. The app can only be run in one pair of hidden and unhidden phones at the same time. Technically, two phones are not paired in any way. The data is broadcasted to all phones running the app. We have two potential ways to fix it. One is using Wifi Peer-to-Peer API to connect two phones Peer-to-Peer without connect to internet and the other is making a registration tab to register two phones. However, we don’t have two phones with Wifi Peer-to-Peer functionalities. Meanwhile, registration tabs does not make sense in our app because we are doing this to simulate interaction between a paired watch and phone. Registration should not required for a paired watch and phone.

3. The GPS location service sometime very inaccurate, especially indoors or underground.  The precision depends on both devices. The app runs inaccurately if the GPS signal is bad.

4. The power consumption is high because the GPS keeps running even if the program is in background. The GPS refresh rate is high to keep track of children in real time. However, you may not see the high refresh rate because of the network delay.

#VII. Future Improvements
1. If we have a watch with builtin GPS hardware, then a lot of problem will be resolved. Then we can connect phone and watch directly without a hidden phone because we no longer need hidden phone to provide a GPS location. This will make coding much easier and organize because there is no loner interaction in hidden phone and online cloud server. Currently, hidden phones and unhidden phones share the same code although have different responsibilities make it very hard to code. Code directly for a watch with GPS will also resolve the lagging issue because there is no longer so many devices.

2. We need to find a way to make the location measurement more precise. Besides of GPS, we  may need to use WIFI, phone signal, accelerometer， compass, pedometer, compass, etc, all sensors which we can take advantage of to improve the location precision.

3. We may increase the function which alerts the parent if the child is out of the range of a building.

4. We need to make more research on child behavior to make it easier to use for the child.

#VIII. Acknowledgements
1. Google GCM Sample code by Google Inc.     
2. RepeatAction class by Carlos Simões.       
3. Spherical Math class by Google Inc.      
4. We appreciate the people who give us a lot of help on StackOverflow.      
5. We appreciate our friend to support us.      
6. We appreciate our instructor, Valkyrie Savage, Teaching Assistances, Andrew Head and Michelle Nguyen and the reader Diana Wang to give us the great semester. [Link](http://cs160.valkyriesavage.com/people.html)           

  

