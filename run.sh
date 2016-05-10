 #!/bin/bash

function help_text {
  printf "\nRun [options]:\n  compile - Compiles the program.\n"
  printf "  clean - Deletes the build files.\n"
  printf "  server - Runs a server. It requires an argument, the name for the server.\n"
  printf "  client - Runs a client with simple interface.\n"
  printf "  simpletest - Runs a simple test. Simulates a single client making requests.\n"
  printf "  multipletest - Runs a multiple test. Simulates many clients making requests. It requires an argument, the number of clients.\n\n"
}

re='^[0-9]+$'

if [ -z $1 ]; then
  help_text

elif [ $1 == "compile" ]; then
  mvn compile

elif [ $1 == "clean" ]; then
  mvn clean

elif [ $1 == "server" ]; then
    if [ -z $2 ];
        then printf "\tInsufficient number of arguments. Missing name for the server.\n"
        else mvn exec:java -Djava.net.preferIPv4Stack=true -Dexec.mainClass=Server.Server -Dexec.args="$2"
    fi
elif [ $1 == "client" ]; then
    mvn exec:java -Djava.net.preferIPv4Stack=true -Dexec.mainClass=Client.Client

elif [ $1 == "simpletest" ]; then
    mvn exec:java -Djava.net.preferIPv4Stack=true -Dexec.mainClass=Client.SimpleTester

elif [ $1 == "multipletest" ]; then
    if [ -z $2 ]; then
        printf "\tInsufficient number of arguments. Missing number of clients.\n"
        else
            if ! [[ $2 =~ $re ]];
                then printf "\tArgument is not a number.\n"
                else mvn exec:java -Djava.net.preferIPv4Stack=true -Dexec.mainClass=Client.MultipleTester -Dexec.args="$2"
            fi
    fi

fi