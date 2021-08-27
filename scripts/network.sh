docker network create -d ipvlan -o parent=eth0 \
    --subnet 192.168.1.0/24 \
    --gateway 192.168.1.1 \
    -o ipvlan_mode=l2 \
    --ip-range 192.168.1.192/27 \
ipvnet