# Auction System

This auction system was created by me as part of a Distributed Systems module coursework

It uses Java RMI to allow client-server communication. 

It is capable of passive replication allowing new replicas to join and leave due to its fault tolerance system.


To run server:

Make sure you're inside the server folder

rmiregistry

javac FrontEnd.java

javac Replica.java

java FrontEnd

java Replica 1

NOTE: More replicas can be added as needed by assigning a different id number e.g. java Replica 2


To run client:

Copy .class generated files on the server folder to the client folder

javac Client.java

java Client