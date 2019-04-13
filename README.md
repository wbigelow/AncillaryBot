# Ancillary Bot
Welcome to Ancillary, a Discord bot designed for the UW Discord.
Ancillary is coded in Java, which is slightly different than how most Discord bots are done.
The benefit of this is that adding new commands to Ancillary is incredibly easy.
## Running
```sh
./gradlew fullJar
java -jar ./build/libs/ancillary-1.0.jar
```
## How to add a new Command
Ancillary uses [JavaCord](https://javacord.org/wiki/getting-started/welcome/) to send commands to Discord. If the wiki doesn't answer your
questions try the [documentation](https://docs.javacord.org/api/v/3.0.0/overview-summary.html).
### Modules
Ancillary uses modules to encapsulate groups of related commands.
The benefit of this is that a new module can be added under the modules package and Ancillary
will automatically consume it upon startup. Commands under a module can access shared resources under that module,
but can not access other modules. Modules should all implement the Module interface.
### Commands
Commands are subclasses of modules. They can access all resources in the module, and private methods
withing that module. Commands have a name, which is the keyword Ancillary will look for, a description which
will appear in the help message, a permission level which dictates the users who can access it, and an execute method,
which is what is called when someone types in the keyword. Commands should all implement the Command interface.
### Example
Take a look at the PingModule, this is a simple command that just replies with the word pong when invoked.
### Pull Requests
When adding a new module and commands, there should only be an addition of a module under the modules package,
no other modules or files should be modified.