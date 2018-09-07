# Corda 3.0 Network Map

A refactor of a [HTTP-based network map for testing](https://github.com/tomtau/stub-corda-networkmap) :
- Clean architecture with in-memory and database (TODO) repositories.
- Tests (TODO)

## Running
```
gradle run
```

## Building
```
gradle distZip # (or gradle distTar)
```

`build/distributions` will contain a zip file with all dependencies + OS-specific
scripts for running

The server can then be run with:
```
bin/stub-corda-networkmap server <path to yaml config>
```

## Configuration
`expiration` (in seconds) in the yaml config sets the expiration / cache HTTP header
that's then used by Corda nodes how often they'd query the network map.

Additional server properties can be configured using the standard Dropwizard configuration: http://www.dropwizard.io/1.3.0/docs/manual/configuration.html#http

## Connecting nodes

For testing, the temporary procedure is:

1. run all other nodes with the compatibilityZoneURL in their configs
