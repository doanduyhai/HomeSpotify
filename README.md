# HomeSpotify WebApp

Before running the webapp locally, ensure that you have the VM running, e.g. an **active Cassandra** listening on port **9042** at the address **192.168.51.10**.


You may want to build and run the VM first by following the instructions <a href="https://github.com/doanduyhai/HomeSpotify/blob/vagrant_vm_build/README.md" target="_blank">here</a>

Build and run the webapp:

```sh

$ cd HomeSpotify
$ git checkout webapp
$ sbt
> container:start
```

Then connect your browser to <a href="http://localhost:9000/" target="_blank">http://localhost:9000/</a>
