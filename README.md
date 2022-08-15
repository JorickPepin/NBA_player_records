# Records joueur NBA

[![Github All Releases](https://img.shields.io/github/downloads/JorickPepin/Wikipedia-help/total.svg?style=for-the-badge)](https://github.com/JorickPepin/Wikipedia-help/releases/latest/download/RecordsJoueurNBA.jar)

<details open="open">
  <summary><h2 style="display: inline-block">Table des matières</h2></summary>
  <ol>
    <li>
      <a href="#création-de-lexécutable">Création de l'exécutable</a>
      <ul>
        <li><a href="#prérequis">Prérequis</a></li>
	    <li><a href="#compilation">Compilation</a></li>
      </ul>
    </li>
    <li>
      <a href="#présentation">Présentation</a>
      <ul>
        <li><a href="#besoin">Besoin</a></li>
	    <li><a href="#fonctionnement">Fonctionnement</a></li>
	    <li><a href="#utilisation">Utilisation</a></li>
	    <li><a href="#résultat-final">Résultat final</a></li>
      </ul>
    </li>
  </ol>
</details>

## Création de l'exécutable

> Il est possible de récupérer l'exécutable en le téléchargeant directement via [ce lien](https://github.com/JorickPepin/Wikipedia-help/releases/latest/download/RecordsJoueurNBA.jar).

### Prérequis

* Java, version 8 ou ultérieure
* Ant (compilation)

### Compilation

Utilisez la commande `ant` ou `ant -f build.xml` depuis le dossier cloné pour compiler le programme.

L'exécutable `.jar` est créé dans le dossier `build/jar/`. Il peut être exécuté avec la commande `java -jar RecordsNBAWiki.jar`.

Vous pouvez également utiliser la commande `ant run` pour créer et exécuter le `.jar`.

## Présentation

### Besoin

Sur la [Wikipédia francophone](https://fr.wikipedia.org/wiki/Wikip%C3%A9dia:Accueil_principal), la plupart des articles des joueurs NBA possèdent une section *Records sur une rencontre* qui contient les meilleures performances du joueur sur un match ainsi que les nombres de [double-doubles](https://fr.wikipedia.org/wiki/Double-double) et [triple-doubles](https://fr.wikipedia.org/wiki/Triple-double) qu'il a réalisés en carrière.

Ces sections étaient **rarement mises à jour**, sourcées avec des **liens morts** voire **non sourcées** et n'étaient pas toujours **mises en forme** de la même manière, cela nécessitant un travail chronophage. Cet outil répond à ces problèmatiques en minimisant le temps nécessaire à la mise à jour de ces sections.

Exemple d'une section non sourcée et non mise en forme &#8595;\
<img src="https://i.imgur.com/mWyvCxC.png" alt="Exemple d'une mauvaise section" width="500" />

### Fonctionnement

À partir d'un nom donné, le logiciel récupère, via l'API de [Wikidata](https://www.wikidata.org/wiki/Wikidata:Main_Page), la liste des articles de joueur de basket-ball en lien avec ce nom et les propose à l'utilisateur qui peut sélectionner celui qu'il souhaite.

Les meilleures performances du joueur sur une rencontre sont récupérées sur le site [realgm.com](https://basketball.realgm.com/), les nombres de double-doubles et triple-doubles sont récupérés sur le site [espn.com](https://www.espn.com/). Il est donc nécessaire que les identifiants uniques du joueur sur ces deux sites soient présents dans l'élément Wikidata de l'article sélectionné. S'ils sont manquants, le logiciel l'indiquera.

Pour la mise en forme, le logiciel se base sur ce qui était déjà fait le plus souvent sur l'encyclopédie. Les principales règles sont les suivantes :
- nom des équipes en français
- un seul lien interne par colonne sur le nom de l'équipe et la date du match
- si la performance a eu lieu à l'extérieur, le nom de l'équipe est précédé d'un `@`
- la date du match correspond à la date au fuseau horaire local, pas au fuseau français

Si vous voyez un point à améliorer dans la mise en forme, n'hésitez-pas à [ouvrir une issue](https://github.com/JorickPepin/NBA_player_records/issues/new).

### Utilisation

> Un exemple d'utilisation est disponible en images dans le dossier [`/doc/example/`](/doc/example/).

![Screen du logiciel](/doc/example/step-3.png)

Entrez le nom du joueur dans la barre de recherche prévue à cet effet puis, dans la liste en-dessous, sélectionnez celui correspondant à l'article que vous souhaitez mettre à jour.

Le logiciel génère alors le contenu à insérer dans la section *Records sur une rencontre* de cet article. Le contenu est affiché et automatiquement ajouté à votre presse-papier mais vous pouvez le rajouter à tout moment en appuyant sur le bouton *Copier*.

Généralement, il est possible de directement coller l'ensemble du contenu dans la section de l'article. Il existe cependant des cas particuliers :
- les joueurs ayant déjà réalisés des triple-doubles ont parfois une boîte déroulante contenant le détail de ces derniers en bas de la section, attention à ne pas l'enlever ([exemple](https://fr.wikipedia.org/wiki/Jayson_Tatum#Records_sur_une_rencontre_en_NBA)).
- les joueurs ayant un record de franchise ou un record NBA : la cellule du record peut avoir une couleur différente et le contenu de la section peut légèrement varier avec notamment une légende supplémentaire ([exemple](https://fr.wikipedia.org/wiki/Jayson_Tatum#Records_sur_une_rencontre_en_NBA)). Il peut être alors utile d'enlever l'en-tête du contenu en décochant le bouton *En-tête*.
- les joueurs retraités n'ont plus besoin de la dernière ligne correspondant à la date de mise à jour, elle est à enlever manuellement ([exemple](https://fr.wikipedia.org/wiki/Jos%C3%A9_Juan_Barea#Records_sur_une_rencontre_en_NBA)).

Dans tous les cas, afin de prévenir les mauvaises manipulations, il est **fortement recommandé de comparer les changements apportés** à l'article en appuyant sur le bouton *Voir les modifications* de Wikipédia avant de les publier.

### Résultat final

![Screen de la section Records sur une rencontre](/doc/example/step-6.png)
