# Chinese Checkers Web App

A fully functional game of Chinese Checkers, written in Java using the Play and Akka frameworks.

## Requirements

The project requires Java 8 to be installed on your system, as well as the Play framework in version 2.2.6. It also requires a modern web browser to be used in order for the web app to be presented as intended.

## Features

Our implementation of Chinese Checkers is able to:
- handle multiple games simultaneously with variable number players (thanks to the Akka framework and its' superb actor systems)
- add bots to the games, which prove to be quite formidabble enemies when unleashed
- differentiate different players thanks to the nickname system
- allow players to either create their own game rooms or join existing game rooms, which await for being completely filled
- and more!

## Logic

The logic of the game was well thought-out earlier, before being implemented in the web app (see the ChineseCheckers repo, which contains all of the initial history of our effort put into this game).

We've done our best to document all of the code for the future reference.

## Web app

The web app is written in Play 2.2.6 due to the availability of better-documented resources and various example projects, which have proven to be extremely helpful for us.

## User interface

The user interface part was mostly written based on excellent Twitter's Bootstrap, which has allowed us to present almost fully responsive UI to the user.

The game board is created on a canvas by the created JavaScript script.

## Sidenotes

In the future the authors will also try to create a project using the newest Play (2.6.10) and Akka framework versions.
