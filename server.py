import time
import socket
from multiprocessing import Process, Manager
import tkinter as tk
from gpiozero import LightSensor

HEADER = 64
PORT = 5050
IP = socket.gethostbyname(socket.gethostname())
ADDRESS = ("", PORT)
FORMAT = "utf-8"
DISCONNECT_MESSAGE = "!DISCONNECT"

LDR_PIN = 4

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
			msg_length = client.recv(HEADER).decode(FORMAT)
		except ConnectionResetError:
			break

		#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		# TODO DONT LET THE CLIENT CRASH THIS!!!
		#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		msg_length = int(msg_length)
		msg = client.recv(msg_length).decode(FORMAT)
		if msg == DISCONNECT_MESSAGE:
			connected = False
		elif msg == "!RESET":
			global_namespace.number_of_people = 0
		elif msg.split()[0] == "!SET":
			global_namespace.number_of_people = int(msg.split()[1])
		print(f"[{address[0]}:{address[1]}] {msg}")

	for i, c in enumerate(clients):
		if c[0] == address:
			del clients[i]
			break
		
def broadcast(clients, global_namespace):
	while True:
		for client in clients:
			send(client[1], str(global_namespace.number_of_people))
		time.sleep(0.2)
		
# def gui(global_namespace):
# 	root = tk.Tk()
# 	def increment():
# 		global_namespace.number_of_people += 1
# 	btn1 = tk.Button(root, text = "Increment", command = increment)
# 	btn1.pack()
# 	root.mainloop()

def check_ldr(global_namespace):
	ldr = LightSensor(LDR_PIN)
	LIGHT_THRESHOLD = 0.4
	intersection = False
	intersection_previous = intersection

	while True:
		intersection = ldr.value < LIGHT_THRESHOLD

		if intersection and not intersection_previous:
			global_namespace.number_of_people += 1

		intersection_previous = intersection
		time.sleep(0.05)

if __name__ == "__main__":

	server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	server.bind(ADDRESS)

	with Manager() as manager:

		clients = manager.list()
		processes = []

		global_namespace = manager.Namespace()
		global_namespace.number_of_people = 0

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