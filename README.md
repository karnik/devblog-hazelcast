# devblog Hazelcast
Bei der Migration von Mainframe Anwendungen in eine Serviceorientierte Architektur, stellen einen die meist rechenintensiven und damit häufig auch langläufigen Batch-Verarbeitungen bzw. Tasks vor mindestens zwei architektonische Grundsatzfragen.

1. Wie definiert man die Schnittstelle, mit der die Steuerung und Überwachung von langläufigen Tasks gewährleistet werden kann?
2. Wie gewährleistet man, dass die Leistungsfähigkeit des neuen Systems ähnlich skaliert wie beispielsweise die eines Parallel Sysplex der Mainframe-Welt.

Dieses Repository enthält eine Beispiel-Implementierung eines Lösungsvorschlags (siehe [adesso Blog](https://www.adesso.de/de/news/blog/distributed-execution-mit-hazelcast.jsp)) für diese kombinierte Fragestellung.

## Build
Bauen der Spring Boot Anwendung:
```bash
$ gradle clean bootJar
```

Anschließend liegt unter ```build/libs/``` das FatJar ```devblog-hazelcast-<version>.jar```.

## Run
**1\.** Starten von drei Nodes mit jeweils einem dedizierten Server-Port für den HTTP-Server:

```bash
# Konsole 1
$ java -jar build/libs/devblog-hazelcast-<version>.jar --server.port=8080

# Konsole 2
$ java -jar build/libs/devblog-hazelcast-<version>.jar --server.port=8081

# Konsole 3
$ java -jar build/libs/devblog-hazelcast-<version>.jar --server.port=8082
```



Nach dem Start erkennt man in **allen** Konsolen, dass die Nodes einen Cluster gebildet haben. Anhand des **this** erkennt man zudem die Node, die zu der aktuellen Konsole gehört.
```bash
Members {size:3, ver:2} [
 Member [127.0.0.1]:5701 - 5ee54b4d-5957-4b8b-9dc1-6c106e9a0f2a this
 Member [127.0.0.1]:5702 - 34e82cd3-77cc-46cc-9ba9-947b04b54b24
 Member [127.0.0.1]:5703 - cc0a6ba7-6d99-4bc4-b9e4-a6d5afd7de27
]
```

**2\.** Starten einer Task für **Node 1** (z.B. mit dem [httpie-Tool](https://httpie.org/)):
```bash
http POST http://localhost:8080/fibonacci/ n=10
```
 
Die Status-Wechsel der Task erkennt man daraufhin an den folgenden Log-Meldungen:
```bash
# Task wurde erzeugt - Status wurde durch Client in Node 1 geschrieben
TaskMapListener  : FibonacciTaskStatus with ID a3d0f6a9-a28b-4978-8123-e0e3b0606b4b added by Member [127.0.0.1]:5701 - 5ee54b4d-5957-4b8b-9dc1-6c106e9a0f2a this

# Task wurde gestartet - Status wurde durch die Task selbst in Node 3 aktualisiert
TaskMapListener  : FibonacciTaskStatus with ID a3d0f6a9-a28b-4978-8123-e0e3b0606b4b updated by Member [127.0.0.1]:5703 - cc0a6ba7-6d99-4bc4-b9e4-a6d5afd7de27. New value: FibonacciTaskStatus(n=10, status=RUNNING, statusMessage=null), Old value: FibonacciTaskStatus(n=10, status=SUBMITTED, statusMessage=null)

# Task wurde erfolgreich beendet - Status wurde durch Callback an Client in Node 1 aktualisiert
TaskMapListener  : FibonacciTaskStatus with ID a3d0f6a9-a28b-4978-8123-e0e3b0606b4b updated by Member [127.0.0.1]:5701 - 5ee54b4d-5957-4b8b-9dc1-6c106e9a0f2a this. New value: FibonacciTaskStatus(n=10, status=FINISHED, statusMessage=null), Old value: FibonacciTaskStatus(n=10, status=RUNNING, statusMessage=null)
```
