# Architecture

:heavy_check_mark:_(COMMENT) Add a description of the architecture of your application and create a diagram like the one below. Link to the diagram in this document._

![image](https://github.com/user-attachments/assets/778ec23f-2c6a-45da-84df-2d40dbc56791)


Client Apps

Web App (Angular): De front-end van het platform, gebouwd met Angular, waar gebruikers en redacteurs interactie hebben met de verschillende functionaliteiten van het platform.

API Gateway

API Gateway: Het enige toegangspunt voor de Web App naar de backend-services. Deze gateway fungeert als een omweg naar de juiste microservice en zorgt voor betere beveiliging en monitoring van het verkeer naar de microservices.

Discovery Service

Discovery Service: De serviceregistratie- en -detectiecomponent die ervoor zorgt dat microservices elkaar dynamisch kunnen vinden. Dit maakt het eenvoudiger om nieuwe services toe te voegen of bestaande services bij te werken zonder dat statische configuratie nodig is.

Cloud Services (Microservices)

  AuthService: Beheert de authenticatie en autorisatie van gebruikers, inclusief inlogprocessen en het controleren van toegangsrechten. Deze service slaat gegevens op in een MySQL-database.

PostService: Verantwoordelijk voor het creëren en bewerken van posts. Redacteurs kunnen hiermee nieuwe content maken en aanpassen. De data wordt opgeslagen in een eigen MySQL-database.

ReviewService: Staat redacteurs toe om posts goed te keuren of af te wijzen. Er is communicatie tussen de PostService en ReviewService via Open-Feign voor het opvragen van postgegevens, zonder duplicatie van logica.

CommentService: Laat gebruikers reacties plaatsen op posts. Deze service beheert alle gegevens over reacties en slaat deze op in een eigen MySQL-database.

Config Service

Config Service: Deze service beheert configuratie-instellingen die gedeeld worden door alle microservices. Dit kan bijvoorbeeld gebruikt worden voor het opslaan van gemeenschappelijke configuraties zoals logniveaus en database-URL's.

Event Bus

Event Bus: Een berichtenbus die gebruikt wordt om gebeurtenissen tussen de microservices te verzenden. Hierdoor kunnen services reageren op gebeurtenissen (zoals het publiceren van een nieuwe post of goedkeuring van een post) zonder directe afhankelijkheid van elkaar, wat helpt om de onafhankelijkheid van de services te behouden.

Communicatie en Loggen

Open-Feign: Wordt gebruikt om asynchrone communicatie tussen microservices zoals de PostService en ReviewService mogelijk te maken. Dit biedt een declaratieve manier om REST-clients te maken, waarbij requests en responses asynchroon kunnen worden afgehandeld. Dit verhoogt de modulariteit en zorgt ervoor dat services efficiënter met elkaar communiceren zonder elkaars prestaties te beïnvloeden.

De Message Bus zorgt voor asynchrone communicatie tussen de PostService, ReviewService, en CommentService om directe afhankelijkheden te vermijden:

PostService: Verstuurt berichten naar de Event Bus bij het aanmaken, aanpassen, goedkeuren, of afwijzen van een post. Dit stelt de ReviewService en notificatieservices in staat om op de hoogte te blijven van wijzigingen in posts.

ReviewService: Stuurt berichten wanneer een post wordt goedgekeurd of afgewezen, waardoor de PostService kan bijwerken of publiceren, en redacteurs meldingen ontvangen over de status van hun posts.

CommentService: Verstuurt berichten bij nieuwe reacties of updates aan reacties, zodat de PostService of notificatieservices gebruikers kunnen informeren over nieuwe interacties op posts.

Deze opzet zorgt voor losse koppeling en schaalbaarheid van de services.
LogBack: LogBack moet geïmplementeerd worden in elke microservice om loginformatie zowel op het scherm als naar een bestand te schrijven. Dit maakt debugging en monitoring gemakkelijker.

Test Coverage

Backend Test Coverage (70%): Er wordt een testdekking van 70% voor de backend vereist. Dit houdt in dat de meeste logica en belangrijke functies van de services gedekt moeten zijn door unit- en integratietests.
Frontend Test Coverage (50%): De frontend moet een testdekking van 50% hebben, wat betekent dat de belangrijkste UI-componenten en functies getest moeten worden.

Samenvatting

De architectuur voldoet aan alle vereisten door het gebruik van microservices die onafhankelijk van elkaar werken, Open-Feign voor interne communicatie, een Event Bus voor asynchrone berichten, en LogBack voor loggen. Dit ontwerp biedt schaalbaarheid, eenvoud in onderhoud, en de mogelijkheid om services onafhankelijk te ontwikkelen en te testen.
