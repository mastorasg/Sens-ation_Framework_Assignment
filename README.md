# Sens-ation_Framework_Assignment
University of Bamberg Ubiquitus Assignment #3

# SENS-ATION SENSOR

1. cd into SensationSensor Folder

2. compile SensorRPC.java via the following command with classpath referencing xmlrpc-1.2-b1.jar and SensorUDPReceiver.jar:

        javac -cp lib/xmlrpc-1.2-b1.jar:lib/SensorUDPReceiver.jar -sourcepath src -d bin/classes src/de/cmlab/ubicomp/SensorRPC.java
 
   or simply

        ant compile
        
3. run SensorRPC by executing the following commands:

        jar cfm bin/jar/SensorRPC.jar myManifest -C bin/classes .
        java -cp lib/xmlrpc-1.2-b1.jar:lib/SensorUDPReceiver.jar:bin/jar/SensorRPC.jar de.cmlab.ubicomp.SensorRPC
        
   or simply

        ant run
        
   clean & 2 & 3 using ant:

        ant main
        
   or simply

        ant
        
4. check your console output


# SENS-ATION CLIENT

1. cd into SensationClient Folder

2. compile ClientRPC.java via the following command with classpath referencing xmlrpc-1.2-b1.jar:

        javac -cp lib/xmlrpc-1.2-b1.jar -sourcepath src -d bin/classes src/de/cmlab/ubicomp/ClientRPC.java
 
   or simply

        ant compile
        
3. run ClientRPC by executing the following commands:

        jar cfm bin/jar/ClientRPC.jar myManifest -C bin/classes .
        java -cp lib/xmlrpc-1.2-b1.jar:bin/jar/ClientRPC.jar de.cmlab.ubicomp.ClientRPC
        
   or simply

        ant run
        
   clean & 2 & 3 using ant:

        ant main
        
   or simply

        ant
        
4. check your console output