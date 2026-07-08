# DadaPlatform

**Versione:** `26.1.1`

DadaPlatform e' un piccolo core Java 8 per plugin Minecraft che centralizza l'invio di messaggi formattati e task compatibili tra Spigot, Paper e Folia.

L'obiettivo e' scrivere una sola API nei tuoi plugin e lasciare alla piattaforma il compito di scegliere il backend corretto:

- su Paper moderno usa Adventure nativo quando disponibile;
- su Spigot usa stringhe legacy e API Bukkit/Bungee compatibili;
- su Folia usa gli scheduler entity/region/global quando disponibili;
- i testi possono essere scritti con sintassi MiniMessage;
- sono disponibili messaggi chat, actionbar, title/subtitle e placeholder.

## Requisiti

- Java 8
- Maven
- Spigot/Paper API compatibile con il progetto
- MiniMessage/Adventure disponibili a runtime

> Nota: il core compila contro Spigot API come minimo comune denominatore. Paper e Folia vengono usati tramite rilevamento runtime, cosi puoi tenere un solo jar del plugin.

> Nota: su Paper moderno Adventure e' gia' integrato in molte versioni. Su Spigot, invece, se il tuo plugin finale usa questa libreria devi assicurarti di includere/shadare le dipendenze Kyori necessarie, oppure renderle disponibili in altro modo.

## Installazione

Se lo usi come dipendenza Maven locale:

```bash
mvn clean install
```

Poi nel plugin che usa il core:

```xml
<dependency>
    <groupId>me.mrbast</groupId>
    <artifactId>Platform</artifactId>
    <version>26.1.1</version>
</dependency>
```

Se lo pubblichi su GitHub e vuoi usarlo tramite JitPack, aggiungi il repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

E poi usa le coordinate generate da JitPack per il tuo repository.

## Inizializzazione

Nel main class del tuo plugin prepara la piattaforma una volta in `onEnable`.

```java
import me.mrbast.platform.Platform;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {

    private Platform platform;

    @Override
    public void onEnable() {
        this.platform = Platform.prepare(this);
    }

    public Platform getPlatform() {
        return platform;
    }
}
```

`Platform.prepare(this)` rileva automaticamente se il server supporta le API Adventure di Paper. In caso contrario usa il backend Spigot.

Puoi controllare il tipo di ambiente:

```java
if (platform.isFolia()) {
    getLogger().info("Server Folia rilevato");
}

if (platform.hasPaperAdventure()) {
    getLogger().info("Adventure Paper disponibile");
}
```

## Messaggi

Puoi inviare un messaggio a un `Player` o a un qualsiasi `CommandSender`.

```java
platform.sendMessage(player, "<green>Ciao <white>player!");
platform.sendMessage(sender, "<red>Non hai il permesso.");
```

Oppure puoi usare il `Formatter`.

```java
platform.getFormatter("<gold>Benvenuto nel server!")
        .send(player);
```

## Placeholder

`Format` permette di sostituire placeholder nel formato `%nome%`.

```java
import me.mrbast.platform.format.Format;

Format format = Format.of("player", "coins")
        .as(player::getName, () -> String.valueOf(1250));

platform.getFormatter("<green>Ciao %player%, hai <yellow>%coins% <green>coins.")
        .send(format, player);
```

Puoi anche ottenere la stringa formattata:

```java
String message = platform.getFormatter("<gray>Player: <white>%player%")
        .get(format);
```

## Actionbar

Invio diretto:

```java
platform.sendActionBar(player, "<yellow>Salvataggio completato");
```

Con `Formatter`:

```java
platform.getFormatter("<aqua>%player% <gray>sta entrando...")
        .sendActionBar(Format.of("player").as(player::getName), player);
```

Sono disponibili entrambi i nomi:

```java
platform.sendActionbar(player, "<green>Ok");
platform.sendActionBar(player, "<green>Ok");
```

## Title e subtitle

Con tempi default:

```java
platform.sendTitle(player, "<gold>Level up!", "<gray>Hai raggiunto il livello 10");
```

Con tempi personalizzati in tick:

```java
platform.sendTitle(
        player,
        "<gold>Level up!",
        "<gray>Hai raggiunto il livello 10",
        10,
        60,
        10
);
```

Con `Formatter`:

```java
platform.getFormatter("<gold>%title%")
        .sendTitle(
                Format.of("title", "level").as(() -> "Level up!", () -> "10"),
                "<gray>Hai raggiunto il livello %level%",
                10,
                60,
                10,
                player
        );
```

## Lore

`LoreUtil` converte una stringa multilinea in una lista di righe.

```java
import me.mrbast.platform.util.LoreUtil;

List<String> lore = LoreUtil.toLore(
        platform.getFormatter("<gray>Riga 1\n<yellow>Riga 2").get()
);
```

## Scheduler compatibili con Folia

Su Folia non devi trattare il server come se avesse un unico main thread globale. Per operazioni legate a player, entita' o blocchi usa gli scheduler del core.

Task globale:

```java
platform.runGlobal(() -> {
    // Logica globale non legata a una posizione specifica
});
```

Task asincrono:

```java
platform.runAsync(() -> {
    // Operazioni I/O, database, chiamate web, ecc.
});
```

Task su un player o entita':

```java
platform.runFor(player, () -> {
    platform.sendMessage(player, "<green>Operazione completata");
});
```

Task su una posizione:

```java
platform.runAt(location, () -> {
    location.getBlock().setType(Material.DIAMOND_BLOCK);
});
```

Task ritardati:

```java
platform.runForLater(player, () -> {
    platform.sendActionBar(player, "<yellow>Pronto!");
}, 20L);

platform.runAtLater(location, () -> {
    location.getBlock().setType(Material.AIR);
}, 100L);
```

Su Spigot e Paper non-Folia questi metodi usano il `BukkitScheduler`. Su Folia usano gli scheduler corretti: entity, region, global e async.

## MiniMessage

I testi accettano la sintassi MiniMessage:

```java
platform.sendMessage(player, "<green>Successo");
platform.sendMessage(player, "<#ff8800>Colore HEX");
platform.sendMessage(player, "<bold><red>Attenzione!");
```

Su Spigot il testo viene convertito in formato legacy prima dell'invio.

## API principali

```java
Platform platform = Platform.prepare(plugin);

platform.format("<green>Testo");
platform.sendMessage(player, "<green>Messaggio");
platform.sendMessage(sender, "<red>Errore");
platform.sendActionBar(player, "<yellow>Actionbar");
platform.sendTitle(player, "<gold>Titolo", "<gray>Sottotitolo");
platform.sendTitle(player, "<gold>Titolo", "<gray>Sottotitolo", 10, 70, 20);
platform.runFor(player, () -> platform.sendMessage(player, "<green>Task sicuro per Folia"));

platform.getFormatter("<green>Ciao %name%")
        .send(Format.of("name").as(player::getName), player);
```

## Build

```bash
mvn clean package
```

Il jar viene generato in:

```text
target/Platform-26.1.1.jar
```

## Compatibilita

Il progetto e' compilato per Java 8:

```xml
<maven.compiler.source>8</maven.compiler.source>
<maven.compiler.target>8</maven.compiler.target>
```

Il backend Paper viene scelto solo se il server espone i metodi Adventure necessari. In caso contrario viene usato il backend Spigot.

Per dichiarare un plugin compatibile con Folia devi aggiungere anche questo nel tuo `plugin.yml` o `paper-plugin.yml` del plugin finale:

```yaml
folia-supported: true
```

Questo flag da solo non basta: il codice deve usare scheduler compatibili con Folia quando tocca entita', player, blocchi o regioni. Per questo il core espone `runFor`, `runAt`, `runGlobal` e `runAsync`.
