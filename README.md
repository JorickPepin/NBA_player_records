
**README en cours de réécriture...**

---

[![Github All Releases](https://img.shields.io/github/downloads/JorickPepin/Wikipedia-help/total.svg?style=for-the-badge)](https://github.com/JorickPepin/Wikipedia-help/releases/latest/download/RecordsJoueurNBA.jar)

<details open="open">
  <summary><h2 style="display: inline-block">Table des matières</h2></summary>
  <ol>
    <li>
      <a href="#pour-commencer">Pour commencer</a>
      <ul>
        <li><a href="#prérequis">Prérequis</a></li>
	<li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li>
      <a href="#présentation">Présentation</a>
      <ul>
        <li><a href="#besoin">Besoin</a></li>
	<li><a href="#fonctionnement">Fonctionnement</a></li>
      </ul>
    </li>
  </ol>
</details>

## Pour commencer

### Prérequis

* Java, version 8 ou ultérieure
* Ant (compilation)

### Installation

Utilisez la commande ```ant``` ou ```ant -f build.xml``` depuis le dossier cloné pour compiler le programme.

L'exécutable ```.jar``` est créé dans le dossier ```build/jar/```.

Vous pouvez également utiliser la commande ```ant run``` pour créer et exécuter le ```.jar```.

## Présentation

### Besoin

Sur la [Wikipédia francophone](https://fr.wikipedia.org/wiki/Wikip%C3%A9dia:Accueil_principal), les articles des joueurs NBA possèdent souvent une section *Records sur une rencontre* qui contient les meilleures performances du joueur sur un match ainsi que les nombres de [double-doubles](https://fr.wikipedia.org/wiki/Double-double) et [triple-doubles](https://fr.wikipedia.org/wiki/Triple-double) qu'il a réalisés en carrière.

Je me suis rendu compte que ces sections étaient **rarement mises à jour**, régulièrement sourcées avec des **liens morts** voire **non sourcées** et avaient parfois une **mise en forme rebutante**. Cet outil permet donc de répondre à ces problèmatiques-là en peu de temps si on le compare au temps nécessaire pour le faire manuellement.

Exemple de mise en forme minimaliste &#8595;\
<img src="https://i.imgur.com/mWyvCxC.png" alt="Exemple de mauvaise mise en forme" width="500" />

### Fonctionnement

...

---
Ancienne version :

This program is a help to make **NBA players maintenance** easier on the **French Wikipedia**.

It enables to get the **career bests** and the **number of double-double/triple-double** of an NBA player in a **text file**. The result of the retrieval is formatted to only have to paste it on the encyclopedia.

# Operation
Example with [**Shai Gilgeous-Alexander**](https://fr.wikipedia.org/wiki/Shai_Gilgeous-Alexander) :
## Parameters
The program takes **two parameters** :
- the **RealGM** player ID (career bests)
- the **ESPN** player ID (DD2/TD3)

These two pieces of information are, in most cases, present at the bottom of the player's page in the **external links section** &#8595;\
![External links example](https://i.imgur.com/Zm2aNUI.png)
By clicking on the links, you will find the following two identifiers **in the urls**  :
- **RealGM ID** : 104915
- **ESPN ID** : 4278073

## File
To get the file, all you have to do is run the program and enter the two player IDs found above &#8595;\
![Execution example](https://i.imgur.com/JhbGiHF.png)

If there is no error during the execution, a file in the player's name appears in the *fichiers* folder containing the template &#8595;\
![Fichiers folder example](https://i.imgur.com/nfTuYxA.png)

In [this file](https://github.com/JorickPepin/Wikipedia-help/blob/master/fichiers/Shai_Gilgeous-Alexander.txt), you will find the content to add to the header of the Wikipedia code (a header example is available [here](https://fr.wikipedia.org/w/index.php?title=Utilisateur:Jorlck/Mod%C3%A8les&action=edit&section=4) if the player's page does not yet contain a records section).

The content of the file is formatted so that an internal link to a date or a team name is only created once per section.

The final result is as follows &#8595;\
![Final result example](https://i.imgur.com/GjB9BzQ.png)
