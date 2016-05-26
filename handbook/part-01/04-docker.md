
how to install docker on oel 7 : https://docs.docker.com/engine/installation/linux/oracle/#configure-docker-to-start-on-boot
how to integrate eclipse with docker : https://www.eclipse.org/community/eclipse_newsletter/2015/june/article3.php
how to ensure docker starts with both unix and tcp sockets 
 - cd /lib/systemd/system
 - edit docker.service to update ExecStart element 
 ```
	[Unit]
	Description=Docker Application Container Engine
	Documentation=https://docs.docker.com
	After=network.target docker.socket
	Requires=docker.socket

	[Service]
	Type=notify
	# the default is not to use systemd for cgroups because the delegate issues still
	# exists and systemd currently does not support the cgroup feature set required
	# for containers run by docker
	ExecStart=/usr/bin/docker daemon -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock
	MountFlags=slave
	LimitNOFILE=1048576
	LimitNPROC=1048576
	LimitCORE=infinity
	TimeoutStartSec=0
	# set delegate yes so that systemd does not reset the cgroups of docker containers
	Delegate=yes

	[Install]
	WantedBy=multi-user.target
  ```