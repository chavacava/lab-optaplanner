Toy problem to get the hands on [Optaplanner](https://github.com/kiegroup/optaplanner)

Inputs:
1. Satellite visibilities: a list of opportunities to contact a satellite from a ground antenna. 
Each vibility has the following attributes: time period, satellite id, antenna id
2. Contact requests: a list of requests to contact a satellite.
Each request has the following attributer: required duration, satellite id

Output:
1. Contact plan: a list of (contact request, satellite visibility)

Constraints:
1. Must not assign the same visibility to more than one contact request
1. Must not assign a visibility with a duration shorter than required by the contact request
1. Must not assign a visibility for sat X to a contact request for sat Y
1. Must not assign overlapping visibilities for a sat
1. Must not use the same antenna to stablish more than one contact at the same time

Usage:

```
mvn test
```
```
mvn exec:java
```
