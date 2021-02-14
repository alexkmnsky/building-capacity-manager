import socket
import tkinter as tk
from multiprocessing import Process, Manager
import time

HEADER = 64
IP = "192.168.200.130"
PORT = 5050
ADDRESS = (IP, PORT)
FORMAT = "utf-8"
DISCONNECT_MESSAGE = "!DISCONNECT"

def send(server, msg):
	message = msg.encode(FORMAT)
	msg_length = len(message)
	send_length = str(msg_length).encode(FORMAT)
	send_length += b" " * (HEADER - len(send_length))
	server.send(send_length)
	server.send(message)

def recieve_messages(server, global_namespace):
	while True:
		msg_length = server.recv(HEADER).decode(FORMAT)
		msg_length = int(msg_length)
		msg = server.recv(msg_length).decode(FORMAT)
		global_namespace.number_of_people = int(msg.split()[0])

def gui(server, global_namespace):
	root = tk.Tk()
	def set_number_of_people(value):
		send(server, f"!SET {value}")
	def reset():
		send(server, "!RESET")

	btn1 = tk.Button(root, text = "set to 5", command = lambda: set_number_of_people(5))
	btn1.pack()
	btn2 = tk.Button(root, text = "reset", command = reset)
	btn2.pack()
	text_count = tk.Label(root, text = global_namespace.number_of_people, font=("Arial", 400, ""))
	text_count.pack()

	def update():
		text_count.config(text = global_namespace.number_of_people)
		root.after(1, update)

	update()
	
	root.mainloop()

if __name__ == "__main__":

	server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	server.connect(ADDRESS)

	with Manager() as manager:
		global_namespace = manager.Namespace()
		global_namespace.number_of_people = -1
		gui_process = Process(target = gui, args = (server, global_namespace))
		listen_process = Process(target = recieve_messages, args = (server, global_namespace))

		gui_process.start()
		listen_process.start()

		while True:
			time.sleep(1)
			...