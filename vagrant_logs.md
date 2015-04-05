Vagrant logs messages during the VM build

<pre>
Bringing machine 'default' up with 'virtualbox' provider...
==> default: Importing base box 'ubuntu/trusty64'...
==> default: Matching MAC address for NAT networking...
==> default: Checking if box 'ubuntu/trusty64' is up to date...
==> default: Setting the name of the VM: Home Spotify with Spark & Cassandra
==> default: Clearing any previously set forwarded ports...
==> default: Clearing any previously set network interfaces...
==> default: Preparing network interfaces based on configuration...
    default: Adapter 1: nat
    default: Adapter 2: hostonly
==> default: Forwarding ports...
    default: 22 => 1233 (adapter 1)
    default: 22 => 2222 (adapter 1)
==> default: Running 'pre-boot' VM customizations...
==> default: Booting VM...
==> default: Waiting for machine to boot. This may take a few minutes...
    default: SSH address: 127.0.0.1:2222
    default: SSH username: vagrant
    default: SSH auth method: private key
    default: Warning: Connection timeout. Retrying...
    default:
    default: Vagrant insecure key detected. Vagrant will automatically replace
    default: this with a newly generated keypair for better security.
    default:
    default: Inserting generated public key within guest...
    default: Removing insecure key from the guest if its present...
    default: Key inserted! Disconnecting and reconnecting using new SSH key...
==> default: Machine booted and ready!
==> default: Checking for guest additions in VM...
==> default: Setting hostname...
==> default: Configuring and enabling network interfaces...
==> default: Mounting shared folders...
    default: /vagrant => /Users/archinnovinfo/perso/SparkCassandra_Vagrant
==> default: Running provisioner: ansible...
PYTHONUNBUFFERED=1 ANSIBLE_FORCE_COLOR=true ANSIBLE_HOST_KEY_CHECKING=false ANSIBLE_SSH_ARGS='-o UserKnownHostsFile=/dev/null -o ControlMaster=auto -o ControlPersist=60s' ansible-playbook --private-key=/Users/archinnovinfo/perso/SparkCassandra_Vagrant/.vagrant/machines/default/virtualbox/private_key --user=vagrant --connection=ssh --limit='default' --inventory-file=/Users/archinnovinfo/perso/SparkCassandra_Vagrant/.vagrant/provisioners/ansible/inventory playbook.yml

PLAY [all] ********************************************************************

GATHERING FACTS ***************************************************************
ok: [default]

TASK: [tools | Install various tools] *****************************************
changed: [default] => (item=vim,git,nmon,dstat,tar,gzip)

TASK: [java | Install add-apt-repostory] **************************************
ok: [default]

TASK: [java | Add Oracle Java Repository] *************************************
changed: [default]

TASK: [java | Accept Java 8 Licence] ******************************************
changed: [default]

TASK: [java | Install Oracle Java 8] ******************************************
changed: [default] => (item=oracle-java8-installer,ca-certificates,oracle-java8-set-default)

TASK: [cassandra | add cassandra debian repository] ***************************
changed: [default]

TASK: [cassandra | add the key for the cassandra debian repo] *****************
changed: [default]

TASK: [cassandra | add the other key for cassandra] ***************************
changed: [default]

TASK: [cassandra | add the other public key for cassandra] ********************
changed: [default]

TASK: [cassandra | install cassandra] *****************************************
changed: [default]

TASK: [cassandra | override cassandra.yaml file] ******************************
changed: [default]

TASK: [cassandra | make sure cassandra is started] ****************************
ok: [default]

TASK: [cassandra | wait for the HTTP port to be listening] ********************
ok: [default]

TASK: [cassandra | alias for cqlsh to avoid specifying IP address every time] ***
changed: [default]

TASK: [handson-data | copy hands on CSV files] ********************************
changed: [default]

TASK: [handson-data | unzip data files] ***************************************
changed: [default]

TASK: [handson-data | check if data has been imported] ************************
ok: [default]

TASK: [handson-data | insert music data into Cassandra] ***********************
changed: [default]

TASK: [handson-data | insert ratings data into Cassandra] ***********************
changed: [default]

TASK: [handson-app | checkout the app] ****************************************
changed: [default]

TASK: [handson-app | create service for handson-app] **************************
changed: [default]

TASK: [handson-app | build the stand-alone jar (it may take a while because SBT will download 111Mb of dependencies)] ***
changed: [default]

TASK: [handson-app | install homespotify as system service at priority 55 (after Cassandra, which is 50)] ***
changed: [default]

TASK: [handson-app | ensure the homespotify webapp is started] ****************
changed: [default]

PLAY RECAP ********************************************************************
default                    : ok=25   changed=20   unreachable=0    failed=0
</pre>
