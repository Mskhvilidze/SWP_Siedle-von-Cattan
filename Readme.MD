SWP Basissystem
===============

Der folgende Code sollte als Basis für das Software Projekt verwendet werden.


Maven
-----
Das Projekt kann mit Maven gebaut werden.

Dazu auf der Hauptebene zunächst

<pre><code>mvn clean install
</code></pre>

aufrufen.

Um den Client zu bauen:
<pre><code>cd client
mvn clean package
</code></pre>

der Client ist dann unter traget zu finden und kann dann z.B. wie folgt aufgerufen werden:

```cd target
java -jar client-<version in pom>-jar-with-dependencies.jar
```

Für den Server:
Upload vom eigenen Rechner mit
```
scp <Pfad>\swp2020i\server\target\server-<version in pom>-jar-with-dependencies.jar <username>@duemmer.informatik.uni-oldenburg.de:/user/proj/sp20/sp20gi/server<datum>.jar
```

dann starten auf ARBI server nach ssh login mit
```
/usr/local/bin/java11 -jar /user/proj/sp20/sp20gi/server<datum>.jar 50092
```


Weitere Hinweise finden sich im Wiki https://confluence.swl.informatik.uni-oldenburg.de/display/SWP/Vorlesungsvideos


