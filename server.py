import time
import socket
from multiprocessing import Process, Manager
import tkinter as tk
import os

HEADER = 64
PORT = 5050
IP = socket.gethostbyname(socket.gethostname())
ADDRESS = ("", PORT)
FORMAT = "utf-8"
DISCONNECT_MESSAGE = "!DISCONNECT"

LDR_PIN = 4
LIGHT_THRESHOLD = 0.5

SOUND_COMMAND_NOTIFY = "aplay ./Desktop/notify.wav"
SOUND_COMMAND_REGISTER = "aplay ./Desktop/register.wav"

def send(client, message):
	message_encoded = message.encode(FORMAT)
	messsage_length = len(message_encoded)
	send_length = str(messsage_length).encode(FORMAT)
	send_length += b" " * (HEADER - len(send_length))
	client.send(send_length)
	client.send(message_encoded)

def handle_client(client, address, clients, global_namespace):
	print(f"[NEW CONNECTION] {address} connected")

	connected = True
	while connected:
		try:
			msg_length = ""
			while msg_length == "":
				msg_length = client.recv(HEADER).decode(FORMAT)

			msg_length = int(msg_length)
			msg = client.recv(msg_length).decode(FORMAT)
		except ConnectionResetError:
			break
		except ConnectionAbortedError:
			break

		if msg == DISCONNECT_MESSAGE:
			connected = False
		elif msg == "!RESET":
			global_namespace.number_of_people = 0
		elif msg.split()[0] == "!SET":
			global_namespace.number_of_people = max(0, int(msg.split()[1]))
		elif msg.split()[0] == "!MAXIMUM":
			global_namespace.maximum_capacity = int(msg.split()[1])
		elif msg == "!INCREMENT":
			global_namespace.increment = 1
		elif msg == "!DECREMENT":
			global_namespace.increment = -1

		print(f"[{address[0]}:{address[1]}] {msg}")
		time.sleep(0.1)

	for i, c in enumerate(clients):
		if c[0] == address:
			del clients[i]
			break
		
def broadcast(clients, global_namespace):
	while True:
		for client in clients:
			try:
				send(client[1], str(global_namespace.number_of_people) + " " + str(global_namespace.maximum_capacity))
			except:
				pass
		time.sleep(0.2)
		
# def gui(global_namespace):
# 	root = tk.Tk()
# 	def increment():
# 		global_namespace.number_of_people += 1
# 	btn1 = tk.Button(root, text = "Increment", command = increment)
# 	btn1.pack()
# 	root.mainloop()

def check_ldr(global_namespace):
	try:
		from gpiozero import LightSensor
	except ImportError:
		print("Failed to import gpiozero")
	try:
		ldr = LightSensor(LDR_PIN)
		intersection = False
		intersection_previous = intersection

		while True:
			intersection = ldr.value < LIGHT_THRESHOLD

			if intersection and not intersection_previous:
				global_namespace.number_of_people = max(0, global_namespace.number_of_people + global_namespace.increment)
				if global_namespace.number_of_people >= global_namespace.maximum_capacity and global_namespace.increment == 1:
					os.system(SOUND_COMMAND_NOTIFY)
				else:
					os.system(SOUND_COMMAND_REGISTER)

			intersection_previous = intersection
			time.sleep(0.01)
	except:
		print("Lightsensor fail")

if __name__ == "__main__":

	server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	server.bind(ADDRESS)

	with Manager() as manager:

		clients = manager.list()
		processes = []

		global_namespace = manager.Namespace()
		global_namespace.number_of_people = 0
		global_namespace.maximum_capacity = 100
		global_namespace.increment = 1

		print(f"[LISTENING] Server is listening on {IP}:{PORT}")
		
		broadcast_process = Process(target = broadcast, args = (clients, global_namespace))
		broadcast_process.start()

		ldr_process = Process(target = check_ldr, args = (global_namespace,))
		ldr_process.start()

		# gui_process = Process(target = gui, args = (global_namespace,))
		# gui_process.start()

		server.listen()

		while True:

			client, address = server.accept()
			clients.append((address, client))
			process = Process(target = handle_client, args = (client, address, clients, global_namespace))
			process.start()
			processes.append(process)

			print(f"[ACTIVE CONNECTIONS] {len(clients)}")