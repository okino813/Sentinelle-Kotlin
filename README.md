# Sentinelle - Veille sur vous

Sentinelle est une application mobile Android faite avec Kotlin et Jetpack Compose. C'est mon projet de fin de formation pour le titre professionnel de Concepteur Développeur d'Application (2025-2026).

L'idée vient d'un truc simple : j'avais l'habitude de prévenir quelqu'un avant chaque sortie un peu incertaine, mais ça ne garantissait rien en cas de vrai problème. Sentinelle automatise ça — tu définis un minuteur, si tu ne l'annules pas à ton arrivée, tes proches reçoivent automatiquement ta localisation et des enregistrements audio.

## Documents de conception

- [Dossier de conception](https://drive.proton.me/urls/7WS2TS3AD4#ZSd9e1y9Oi7I)
- [Cahier des charges](https://drive.proton.me/urls/EE3GZSMAM8#hXKGoRzZdf8A)
- [Spécifications Fonctionnelles](https://drive.proton.me/urls/A0MB5RN9Y0#9jgiTDlqecmT)
- [Spécifications Techniques](https://drive.proton.me/urls/Q18XDRP328#DqGJAeaxdaB1)
- [Plans de Test](https://drive.proton.me/urls/HJ3Z0BQAZC#07ELOSaDJ4vA)

## Stack

**Application mobile**
- Kotlin + Jetpack Compose
- Android API 24+ (Android 7)

**API**
- Django (Python) — API REST
- MySQL
- Firebase Auth (les tokens sont vérifiés côté API, aucun mot de passe stocké en base)
- Twilio pour les SMS

## Comment ça marche

L'utilisateur lance un minuteur avant de partir. Pendant toute la durée, l'appli tourne en arrière-plan via un Service Android — elle enregistre le micro par cycles de 5 minutes et envoie les coordonnées GPS toutes les 10 secondes. Si la connexion est coupée, les données s'accumulent en file d'attente et sont envoyées dès qu'elle revient.

Si le minuteur arrive à zéro sans être annulé, un SMS est envoyé au contact avec un lien vers une page web qui affiche le trajet et les enregistrements audio.

Il y a aussi un bouton SOS manuel pour envoyer la position instantanément.

## Architecture

Le projet suit une architecture N-tiers : le client Kotlin communique avec une API REST Django qui accède à une base MySQL. Le site web (aussi sous Django) sert principalement à afficher les trajets aux contacts qui reçoivent le lien.

## Déploiement

L'API se déploie automatiquement sur un serveur OVH via GitHub Actions à chaque merge sur main. L'appli Android est publiée manuellement sur le Play Store via un App Bundle signé.

---

*Axel Vincent Jaunet — CDA 2026*