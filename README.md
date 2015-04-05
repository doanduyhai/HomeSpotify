# HomeSpotify VM image build

To build an VM for the hands-on labs, please ensure you have:

* latest version of **VirtualBox**
* latest version of **Vagrant**
* latest version of **Ansible**

Then: 

```sh

$ cd HomeSpotify
$ git checkout vagrant_vm_build
$ vagrant up
```

You may see the <a href="https://github.com/doanduyhai/HomeSpotify/blob/vagrant_vm_build/vagrant_logs.md" target="_blank">following messages</a> during the build of the VM:

> Warning: the step **build the stand-alone jar** may take up to 10 minutes, depending on your Internet connection speed, because SBT will download all the dependencies (~111Mb) to build the back-end web application. Take a coffee and be patient :D

The VM will start a **Cassandra** install and deploy the back-end web app for the exercise, the VM is assigned a fix local IP address : **192.168.51.10**

Once the VM is started, the web app can be reached at: <a href="http://192.168.51.10:9000" target="_blank">http://192.168.51.10:9000</a>. 

The source code for the hands-on exercises are available in the branches **scala** or **java**

> If you want to use the VM directly with **VirtualBox** without using **Vagrant**, you can connect to the VM
with `ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no vagrant@192.168.51.10`. The password is **vagrant**
