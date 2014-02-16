# forking-chess

Right now just an in-progress chess game built with [Om](https://github.com/swannodette/om).

## Running the application

Requires [leiningen](https://github.com/technomancy/leiningen). Run `lein dev` and open a browser to [http://localhost:3000](http://localhost:3000).

## Todo

* Reimplement the rewind behavior to just store move diffs, 
  instead of the whole app state. This will make getting the state
  required for implementing 'en-passant' easier

* Implement en-passant

* Implement "legal moves" as a subset of "possible moves"

* Implement turn restrictions

* Fix css issue with "selection" borders overlapping/colliding

* "Fork" a game

* Multiplayer

* Persistence

* Computer opponents
