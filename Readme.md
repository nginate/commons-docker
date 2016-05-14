**Overview**

Official java docker client is a bit creepy when talking about working
with dtos and asynchronous. This lib was made to hide boilerplate when
synchronizing container states and converting client dto classes.

**Build status**

[![][travis img]][travis] [![][jcenterbadge img]][jcenterbadge] [![][codecovbadge img]][codecovbadge] [![][dependencies img]][dependencies]

**What's there in this bundle?**

* Docker client

Wrapper for official docker client. Allows passing dto with null fields 
(official client for some reason has assertions in field setters,
checking against null values). Also hides most of common request 
creation code.

```java
    NDockerClient client = new NDockerClient(dockerClient);
    client.startContainer("id");
    
    // vanilla client
    DockerClient client = ...
    client.startContainerCmd(containerId).exec();
```

* Docker container

All container commands require passing container id, so it could be 
encapsulated in another wrapper.

```java
    DockerContainer service1 = new DockerContainer(client, "id");
    DockerContainer service2 = new DockerContainer(client, "id2");
    
    service1.start();
    service2.start();
    
    service2.stop();
    service2.printLogs();
    
    // vanilla client
    DockerClient client = ...
    String service1Id = ...
    String service2Id = ...
        
    client.startContainerCmd(service1Id).exec();
    client.startContainerCmd(service2Id).exec();
    
    client.stopContainerCmd(service2Id).exec();
    client.logContainerCmd(service2Id)
                    .withStdOut()
                    .withStdErr()
                    .exec(new DockerLogger(logConsumer))
                    .awaitCompletion();
```

**License**

<a href="http://www.wtfpl.net/"><img
       src="http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png"
       width="80" height="15" alt="WTFPL" /></a>
       
[travis]:https://travis-ci.org/nginate/commons-docker
[travis img]:https://travis-ci.org/nginate/commons-docker.svg?branch=master

[jcenterbadge]:https://bintray.com/nginate/maven/commons-docker/_latestVersion
[jcenterbadge img]:https://api.bintray.com/packages/nginate/maven/commons-docker/images/download.svg

[codecovbadge]:https://codecov.io/gh/nginate/commons-docker
[codecovbadge img]:https://codecov.io/gh/nginate/commons-docker/branch/master/graph/badge.svg

[dependencies]:https://www.versioneye.com/user/projects/57372ca1a0ca350034be7c28
[dependencies img]:https://www.versioneye.com/user/projects/57372ca1a0ca350034be7c28/badge.svg?style=flat