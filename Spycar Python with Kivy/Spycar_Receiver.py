'''This Spycar receiver app is intend to be install in Raspberry Pi
This allow the Raspberry Pi to receive the signal from Firebase come from the controller
This app also allows the Raspberry Pi to update the sensor values to Firebase
This app display a basic UI using Kivy'''

# kivy module
from kivy.app import App
from kivy.uix.screenmanager import Screen, ScreenManager
from kivy.lang import Builder
from kivy.core.window import Window
# firebase module
import os
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db


# --------------------------------------------- UI Design -------------------------------------------------------#
# set the window color
Window.clearcolor = (30/255.0, 61/255.0, 78/255.0, 1)

# The screen manager for this app, already handled in Spycar_Receiver.kv
class ScreenManagement(ScreenManager):
    pass


# The main screen for this app
class MainScreen(Screen):
    # hold the reference for the created object
    this = None

    def __init__(self, **kwargs):
        super(MainScreen, self).__init__(**kwargs)
        MainScreen.this = self  # hold the created object

    # upload the sensor distance to the firebase
    def submit(self):
        submitDistance = self.ids.change_distance.text.replace(' ', '')  # get the distance typed
        # update to firebase
        db.reference('sensor_distance').set(submitDistance)

    # Update UI for direction, front_light_on, video_on, and sensor_distance
    @staticmethod
    def updateUI(direction, front_light_on, video_on, sensor_distance):
        MainScreen.this.ids.direction.text = direction
        MainScreen.this.ids.front_light_on.text = str(front_light_on)
        MainScreen.this.ids.video_on.text = str(video_on)
        MainScreen.this.ids.sensor_distance.text = sensor_distance + " cm"


# get the kv layout file as presentation
presentation = Builder.load_file('Spycar_Receiver.kv')


# The Spycar Receiver app
class SpycarReceiver(App):
    def build(self):
        return presentation


# ---------------------------------------- Connect to firebase ----------------------------------------#
# get the current path for the google-services key
dir_path = os.path.dirname(os.path.realpath(__file__))
cred = credentials.Certificate(dir_path + '/spycar-1231c-firebase-adminsdk-varkx-57ce911c70.json')
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://spycar-1231c.firebaseio.com/'
})


# This is the change listener to the firebase, automatically called when data changed
def listener(event):
    # get data from the database
    direction = db.reference('direction').get()
    front_light_on = db.reference('front_light_on').get()
    video_on = db.reference('video_on').get()
    sensor_distance = db.reference('sensor_distance').get()

    # update the UI
    MainScreen.updateUI(direction, front_light_on, video_on, sensor_distance)


# connect database to listener
firebase_admin.db.reference('').listen(listener)


# To Start the app
if __name__ == '__main__':
    SpycarReceiver().run()


