
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
    <li><a href="#présentation">Présentation</a></li>
  </ol>
</details>

## Pour commencer

### Prérequis

* Java, version 8 ou plus
* Ant (compilation)

### Installation

Utilisez la commande ```ant``` ou ```ant -f build.xml``` depuis le dossier cloné pour compiler le programme.

L'exécutable ```.jar``` est créé dans le dossier ```build/jar/```.

Vous pouvez également utiliser la commande ```ant run``` pour créer et exécuter le ```.jar```.

## Présentation

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
