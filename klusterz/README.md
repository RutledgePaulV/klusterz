# klusterz

A Clojure library for secure and reliable communication between multiple 
application instances deployed within the same kubernetes cluster.

## Why would you use this?

If you run multiple pods of the same service within kubernetes 
(to improve availability) but when certain events occur it's important 
that you communicate them to the other pods. 

## License

[Unlicense](http://unlicense.org/)
