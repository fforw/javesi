javesy
======

Javesy is my attempt at creating an entity system in java. 

See http://t-machine.org/index.php/category/entity-systems/ for some 
interesting blog entries about entity systems.

Dependencies
------------

Javesy has one optional dependency which is to the reflections library ( https://github.com/ronmamo/reflections ).
When the reflections library is present, you can use SystemBuilder.buildFromPackage to find automatically
all components in one package and configure a system with it.
