# Word frequency tool

Needs Java 17+.

    javac -d out src/*.java
    java -cp out Gen big 200 200000        # optional: make test files
    java -cp out Main big 50000 time       # dir, min count, search word

Without Gen, use the small `docs` folder:

    java -cp out Main docs 2 file
