# Kafkactl 

<!-- [![GitHub release](https://img.shields.io/github/v/release/michelin/kafkactl?logo=github&style=for-the-badge)](https://github.com/michelin/kafkactl/releases)
[![GitHub commits since latest release (by SemVer)](https://img.shields.io/github/commits-since/michelin/kafkactl/latest?logo=github&style=for-the-badge)](https://github.com/michelin/kafkactl/commits/main)-->

[![GitHub Build](https://img.shields.io/github/actions/workflow/status/michelin/kafkactl/on_push_main.yml?branch=main&logo=github&style=for-the-badge)](https://img.shields.io/github/actions/workflow/status/michelin/kafkactl/on_push_main.yml)
[![GitHub Stars](https://img.shields.io/github/stars/michelin/kafkactl?logo=github&style=for-the-badge)](https://github.com/michelin/kafkactl)
[![GitHub Watch](https://img.shields.io/github/watchers/michelin/kafkactl?logo=github&style=for-the-badge)](https://github.com/michelin/kafkactl)
[![Docker Pulls](https://img.shields.io/docker/pulls/michelin/kafkactl?label=Pulls&logo=docker&style=for-the-badge)](https://hub.docker.com/r/michelin/kafkactl/tags)
[![Docker Stars](https://img.shields.io/docker/stars/michelin/kafkactl?label=Stars&logo=docker&style=for-the-badge)](https://hub.docker.com/r/michelin/kafkactl)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?logo=apache&style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)

**Kafkactl** is the CLI linked with [Ns4Kafka](https://github.com/michelin/ns4kafka). It lets you deploy your Kafka resources using YAML descriptors.

# Table of Contents

* [Download](#download)
* [Install](#install)
  * [Configuration file](#configuration-file)
* [Usage](#usage)

# Download

Kafkactl can be downloaded at https://github.com/michelin/kafkactl/releases and is available in 3 different formats:
- JAR (Java 11 required)
- Windows
- Linux

# Install

Kafkactl requires 3 variables to work:
- The url of Ns4kafka
- Your namespace
- Your security token (e.g., a Gitlab token)
  
These variable can be defined in the dedicated configuration file.

## Configuration file

Create a folder .kafkactl in your home directory:

- Windows: C:\Users\Name\\.kafkactl
- Linux: ~/.kafkactl

Create .kafkactl/config.yml with the following content:

```yaml
kafkactl:
  contexts:
    - name: dev
      context:
        api: https://ns4kafka-dev-api.domain.com
        user-token: my_token
        namespace: my_namespace
    - name: prod
      context:
        api: https://ns4kafka-prod-api.domain.com
        user-token: my_token
        namespace: my_namespace
```

For each context, define your token and your namespace.

Check all your available contexts:

```command
kafkactl config get-contexts
```

Set yourself on a given context:

```command
kafkactl config use-context dev
```

Check your current context:

```command
kafkactl config current-context
```

# Usage

```console
Usage: kafkactl [-hvV] [-n=<optionalNamespace>] [COMMAND]
  -h, --help      Show this help message and exit.
  -n, --namespace=<optionalNamespace>
                  Override namespace defined in config or yaml resource
  -v, --verbose   ...
  -V, --version   Print version information and exit.
Commands:
  apply           Create or update a resource
  get             Get resources by resource type for the current namespace
  delete          Delete a resource
  api-resources   Print the supported API resources on the server
  diff            Get differences between the new resources and the old resource
  reset-offsets   Reset Consumer Group offsets
  delete-records  Deletes all records within a topic
  import          Import resources already present on the Kafka Cluster in
                    ns4kafka
  connectors      Interact with connectors (Pause/Resume/Restart)
  schemas         Update schema compatibility mode
  reset-password  Reset your Kafka password
  config          Manage configuration
```

## Config

This command allows you to manage your Kafka contexts.

```console
Usage: kafkactl config [-v] [-n=<optionalNamespace>] <action> <context>
Manage configuration
      <action>    (get-contexts | current-context | use-context)
      <context>   Context
  -n, --namespace=<optionalNamespace>
                           Override namespace defined in config or yaml resource
  -v, --verbose            ...
```

## Apply

This command allows you to deploy a resource.

```console
Usage: kafkactl apply [-Rv] [--dry-run] [-f=<file>] [-n=<optionalNamespace>]
Create or update a resource
      --dry-run       Does not persist resources. Validate only
  -f, --file=<file>   YAML File or Directory containing YAML resources
  -n, --namespace=<optionalNamespace>
                      Override namespace defined in config or yaml resource
  -R, --recursive     Enable recursive search of file
  -v, --verbose       ...
```

Resources have to be described in yaml manifests.

### Topic

```yaml
---
apiVersion: v1
kind: Topic
metadata:
  name: myPrefix.topic
spec:
  replicationFactor: 3
  partitions: 3
  configs:
    min.insync.replicas: '2'
    cleanup.policy: delete
    retention.ms: '60000'
```

- **metadata.name** must be part of your allowed ACLs. Visit your namespace ACLs to understand which topics you are allowed to manage.
- **spec** properties and more importantly **spec.config** properties validation dependend on the topic validation rules associated to your namespace.
- **spec.replicationFactor** and **spec.partitions** are immutable. They cannot be modified once the topic is created.

### ACL

In order to provide access to your topics to another namespace, you can add an ACL using the following example, where "daaagbl0" is your namespace and "dbbbgbl0" the namespace that needs access your topics.


```yaml
---
apiVersion: v1
kind: AccessControlEntry
metadata:
  name: acl-topic-a-b
  namespace: daaagbl0
spec:
  resourceType: TOPIC
  resource: aaa.
  resourcePatternType: PREFIXED
  permission: READ
  grantedTo: dbbbgbl0
```

- **spec.resourceType** can be TOPIC, GROUP, CONNECT, CONNECT_CLUSTER.
- **spec.resourcePatternType** can be PREFIXED, LITERAL.
- **spec.permission** can be READ, WRITE.
- **spec.grantedTo** must reference a namespace on the same Kafka cluster as yours.
- **spec.resource** must reference any “sub-resource” that you are owner of. For example, if you are owner of the prefix “aaa”, you can grant READ or WRITE access such as:
  - the whole prefix: “aaa” 
  - a sub prefix: “aaa_subprefix”
  - a literal topic name: “aaa_myTopic”

### Connector

```yaml
---
apiVersion: v1
kind: Connector
metadata:
  name: myPrefix.myConnector
spec:
  connectCluster: myConnectCluster
  config:
    connector.class: myConnectorClass
    tasks.max: '1'
    topics: myPrefix.myTopic
    file: /tmp/output.out
    consumer.override.sasl.jaas.config: o.a.k.s.s.ScramLoginModule required username="<user>" password="<password>";
```

- **spec.connectCluster** must refer to one of the Kafka Connect clusters declared in the Ns4Kafka configuration, and authorized to your namespace. It can also refer to a Kafka Connect cluster that you self deployed or you have been granted access.
- Everything else depend on the connect validation rules associated to your namespace.
