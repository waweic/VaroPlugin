## Contributing
Richtilinien zum Programmieren:
- **Ausschließlich** objektorientierte Programmierung (Ausnahme: Utils)
- Comments, Variablen, Methoden, System-outs, ... (Alles, was nicht an die user gerichtet ist) auf **Englisch**
- Camel Case-Notation
- Keine Fehler im Code nach Wechseln der Bukkit oder Spigot API (Reflections)
- Variablen nutzen, keine Config-Zugriffe während des Spielens
- Leerzeilen möglichst vermeiden -> Zeilen sinngemäß anordnen
- Keine Initialisierung der Variablen (außer ggf. final) in der Deklarierung
- Fields mit Annotations vor allen anderen mit Leerzeile
- Getter-Setter unten und nacheinander - Methoden sinngemäß und nach reihenfolge sortieren
- Sortierung nach Sichtbarkeit
  <ol style="list-style-type:square;">
    <li>Private</li>
    <li>Protected</li>
    <li>Package</li>
    <li>Public</li>
  </ol>
- Sortierung der Methoden:
  <ol style="list-style-type:square;">
    <li>Types</li>
    <li>Static Fields</li>
    <li>Static Intializers</li>
    <li>Fields</li>
    <li>Initializers</li>
    <li>Constructors</li>
    <li>Methods</li>
    <li>Static Methods</li>
  </ol>

<br>

**Richtlinien für GitHub:**
- Commits auf Englisch
- **Niemals** auf den master pushen
- Commit-Nachrichten kurz, sinngemäß und effizient beschreiben

**Bei Missachtung der Richtlinien werden pushes verweigert und Rechte entzogen**
