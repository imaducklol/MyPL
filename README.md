# MyPL

### Project Description:
A compiler for MyPL (a fun, simple, made-up language)

The compiler is separated into 6 main pieces
 - Lexer
 - Parser
 - AST Generation
 - Static Checking
 - Code Gen
 - Virtual Machine

For the extension, I implemented multithreading!
The sytax is uses two new functions:

`int thread_create(funcName: string, input: someStruct)`

`int thread_wait(tid: int)`

`thread_create` returns the tid for `thread_wait`

`thread_wait` returns the return value of the called function. (Must be an int function)

Youtube Video: https://youtu.be/px0on2SM6uU

Because testing of multithreaded programs is dificult, the new unit test is purely checking whether or not it can still compile and run using my new functions. It's output is effectively random!

Credit to Dr. Bowers for tests and general guidance through the project. 
