#!/bin/bash

#!/bin/bash

# Run Service 1 in a new terminal window
gnome-terminal -- bash -c "cd ./backend && ./gradlew bootRun; exec bash"

# Run Service 2 in a new terminal window
gnome-terminal -- bash -c "sudo ./frontend/keycloak-24.0.1/bin/kc.sh start-dev; exec bash"

# Run Service 3 in a new terminal window
gnome-terminal -- bash -c "cd ./frontend/vue-tfg/ && npm run dev; exec bash"

cd vue-tfg/ && code .

cd ../backend && code .

# #Service 1
# cd ./backend
# sudo ./gradlew bootRun

# #Service 2
# sudo ./keycloak-24.0.1/bin/kc.sh start-dev

# #Service 3
# cd vue-tfg/
# npm run dev
