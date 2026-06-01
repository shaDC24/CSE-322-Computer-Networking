import paho.mqtt.client as mqtt

broker = "broker.hivemq.com"
topic = "buet/cse/2105123/led" # TODO: Put the same topic you used in the ESP code

client = mqtt.Client()
client.connect(broker)

# TODO: the following is an example of publishing a message. You have to modify it so that the python code will run infinitely and wait for input from keyboard. If user presses 'y', it will send "ON"; it will send "OFF" if 'n' is pressed. The program will terminate if user presses 'q'.
# client.publish(topic, "ON")
while True:
    input_user=input("Enter y/n/q : ")
    if input_user == 'y':
        client.publish(topic, "ON")
    elif input_user == 'n':
        client.publish(topic, "OFF")    
    elif input_user == 'q':
        break
    else :
        print("Only y/n/q is valid for this assignment")
          

