@echo off
START "server" java -jar "..\server\target\server-1.0.0-jar-with-dependencies.jar"

START "cmd_test1" java -jar "..\client\target\client-2.0.0-jar-with-dependencies.jar" --login test1 test1
START "cmd_test2" java -jar "..\client\target\client-2.0.0-jar-with-dependencies.jar" --login test2 test2
START "cmd_test3" java -jar "..\client\target\client-2.0.0-jar-with-dependencies.jar" --login test3 test3
START "cmd_test4" java -jar "..\client\target\client-2.0.0-jar-with-dependencies.jar" --login test4 test4

