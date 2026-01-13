# README – Communication TCP avec Sockets Java (Master 1 - Programmation Java Distribuée)

## Description

Ce projet illustre une communication réseau **client–serveur** utilisant le protocole **TCP** et les **sockets Java**.
Le serveur écoute sur un port et accepte des connexions, les clients se connectent et échangent des données de manière fiable.

## Technologies

* Java
* TCP (`Socket`, `ServerSocket`)
* Communication réseau point-à-point

## Compilation

```bash
javac DirectorySyncServer.java
javac DirectorySyncClient.java

```

## Exécution

### Lancer le serveur

```bash
java DirectorySyncServer --port=12345
```

### Lancer un client

```bash
java DirectorySyncClient --serverIp=127.0.0.1 --serverPort=12345
```

## Fonctionnement

* TCP garantit la **fiabilité**, l’**ordre** et l’**intégrité** des messages
* Les échanges se font via des flux (`InputStream` / `OutputStream`)
* Chaque client établit une connexion dédiée avec le serveur

## Remarque

TCP est utilisé ici comme **mécanisme de communication**, la logique applicative est gérée au niveau du code Java.

## Dossiers de test

Le projet utilise deux dossiers locaux pour tester la synchronisation :

* `test_c/` → dossier client
* `test_s/` → dossier serveur

### Contenu

Ces dossiers doivent contenir des fichiers simples de test, par exemple :

* fichiers texte (`a.txt`, `b.txt`)
* versions différentes d’un même fichier
* fichiers présents uniquement côté client ou côté serveur

Ils permettent de vérifier que la logique de synchronisation détecte correctement les différences et applique les actions nécessaires.

> Remarque : ces dossiers servent uniquement aux tests locaux et peuvent être ignorés par Git via `.gitignore`.

---
