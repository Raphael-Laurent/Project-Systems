# Hotel Booking System

Projet réalisé dans le cadre du cours OO Systems Development à l'Efrei.
Application de réservation d'hôtel construite avec deux microservices Spring Boot communiquant en **REST** et **gRPC**, chacun avec sa propre base **H2**.

## Lien Github

https://github.com/Raphael-Laurent/Project-Systems.git

## Architecture

```text
JavaFX Client
|
|HTTP/REST(JSON)
v
booking-service <-- API REST
- DB: bookings (H2)
    |
    |gRPC
    v
    room-inventory-service <-- gère les chambres et leur disponibilité
- DB: rooms (H2)
```

- **`booking-service`** : service REST, point d'entrée du client, gère les réservations (création, consultation, annulation)
- **`room-inventory-service`** : service gRPC, vérification et réservation des chambres (types, prix et disponibilités des chambres)
- **`proto`** : contrat gRPC entre les deux services
- **`javafx-client/`** : interface client desktop

Chaque service contient sa propre base de donnée (H2) pour séparer les responsabilités des deux microservices.

## Structure du projet

```text
proto/                      contrat .proto
booking-service/            API REST + client gRPC
room-inventory-service/     serveur gRPC
javafx-client/                     client desktop
```

Chaque service suit une architecture en couches :

| Couche | booking-service | room-inventory-service |
|---|---|---|
| Web | `web/` contrôleurs REST | `grpc/` implémentation du service gRPC |
| Service | `service/` BookingService (interface + impl) | `service/` RoomInventoryService (interface + impl) |
| Persistance | `repository/` Spring Data | `repository/` Spring Data |
| Entités | `entity/` Booking | `entity/` Room, RoomType, RoomReservation |
| Erreurs | `exception/` erreurs métier, centralisées dans un `@RestControllerAdvice` | — |

Les dépendances sont injectées par Spring via les constructeurs (IoC).
L'API REST est stateless : le serveur ne garde aucune donnée en mémoire.
## Contrat gRPC

Méthodes implémentées dans `room-inventory-service` puis appelée dans `booking-service` via son client gRPC

| RPC | Requête | Réponse | Comportement |
|---|---|---|---|
| `ListAvailableRooms` | `DateRange` | `RoomList` | Chambres sans réservation qui chevauche les dates, avec le prix total du séjour |
| `ReserveRoom` | `RoomRequest` | `RequestResponse` | `success=false` + message si la chambre est déjà réservée ou inconnue |
| `ReleaseRoom` | `RoomRequest` | `RequestResponse` | Supprime la réservation correspondante |


## API REST (http://localhost:8080)

| Méthode | URL | Description | Succès | Erreurs |
|---|---|---|---|---|
| GET | `/rooms/available?checkIn=&checkOut=` | Liste les chambres disponibles | 200 | 400, 503 |
| POST | `/bookings` | Crée une réservation | 201 | 400, 409, 503 |
| GET | `/bookings/{id}` | Consulte une réservation | 200 | 404 |
| DELETE | `/bookings/{id}` | Annule une réservation (libère la chambre) | 200 | 404, 409, 503 |

## Lancer le projet

Prérequis : Java 17

Démarrer le serveur gRPC :
```bash
cd room-inventory-service
./gradlew.bat bootRun

```

Dans un autre terminal, démarrer l'API REST
```bash
cd booking-service
./gradlew.bat bootRun
```

## Client JavaFX

`javafx-client/` est une interface qui permet trois actions :
- **Chercher les chambres disponibles** : `Get /rooms/available`, affiche la liste des chambres disponibles avec les dates saisies. Les chambres sont affichées sous forme de tableau avec leur id, leur type, et le coût total du séjour.
- **Réserver** : `POST /bookings`, l'utilisateur renseigne son nom et l'id de la chambre, et une réservation est créée dans les dates entrées auparavant. La réservation s'affiche dans le log où l'utilisateur peut voir son id.
- **Annuler une réservation** : `DELETE /bookings/{id}`, l'utilisateur entre l'id du booking qu'il souhaite annulé.

Les erreurs serveurs et les réussites s'affichent dans le log de l'app, dans le bas de la fenêtre.

Les deux services doivent déjà tourner avant de lancer le client :

```bash
cd javafx-client
./gradlew.bat run
```

## Equipe
- Arnaud LABERNARDIERE
- Raphaël LAURENT