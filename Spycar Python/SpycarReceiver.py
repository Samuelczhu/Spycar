# firebase module
import os
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db


# ---------------------------------------- Connect to firebase ----------------------------------------#
# This is the change listener to the firebase, automatically called when data changed
def listener(event):
    # get data from the database
    direction = db.reference('direction').get()
    front_light_on = db.reference('front_light_on').get()
    video_on = db.reference('video_on').get()
    sensor_distance = db.reference('sensor_distance').get()

    # print the information
    printInfo(direction, front_light_on, video_on, sensor_distance)


# print information for direction, front_light_on, video_on, and sensor_distance
def printInfo(direction, front_light_on, video_on, sensor_distance):
    print('direction: '+direction)
    print('front_light_on: ' + str(front_light_on))
    print('video_on: ' + str(video_on))
    print('sensor_distance (cm): ' + sensor_distance)
    print("\n\n")


if __name__ == '__main__':
    # get the current path for the google-services key
    dir_path = os.path.dirname(os.path.realpath(__file__))
    cred = credentials.Certificate(dir_path + '/spycar-1231c-firebase-adminsdk-varkx-57ce911c70.json')
    firebase_admin.initialize_app(cred, {
        'databaseURL': 'https://spycar-1231c.firebaseio.com/'
    })

    # connect database to listener
    firebase_admin.db.reference('').listen(listener)