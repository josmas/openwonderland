
Setup
-----

Edit props/wonderland-server.properties to point to the wonderland server you are testing. By default the
system will test localhost:1139

Edit build.properties and set the name of the master test server (this is the machine administoring the test, not the 
Darkstar server).

Test Execution
--------------

Current the harness is hard coded to run the SimpleTestDirector and the test details are hard coded in the
SimpleTestDirector.java file.

To run the test;

ant run-master   the master test manager
ant run-slave    this can be run multiple times 
ant run-manager  optional ui for managing tests, limited functionality at this time


JTRunner
--------

TODO
