# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.host_name = "cassandra-spark-handson"
  config.vm.network :private_network, ip: "192.168.51.10"
  config.vm.network :forwarded_port, guest: 22, host: 1233

  config.vm.provision :ansible do |ansible|
    ansible.playbook = "playbook.yml"
  end

  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id, "--memory", "4048"]
    vb.cpus = 2
    vb.name = "Home Spotify with Spark & Cassandra"
  end

end
