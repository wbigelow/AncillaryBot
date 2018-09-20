# Ancillary Bot
Welcome to Ancillary, a Discord bot that is super customizable.
Ancillary is coded in Java, which is slightly different than how most Discord bots are done.
The benefit of this is that adding new commands to Ancillary is incredibly easy.
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
For more examples, look at the "uw" git branch, many of these commands are specific to a different server, so don't try pulling that branch.
### Running Ancillary
Since Ancillary uses gradle, when you clone Ancillary and build it using your IDE (I recommend IntelliJ IDEA),
it will create ancillary-1.0.jar under the build/libs directory. You can then run it by running the bash command
`java -jar ancillary-1.0.jar YOUR_DISCORD_TOKEN_HERE`. Note that you need to host Ancillary yourself and need a discord bot
token, which you can learn how to create by doing a quick google search.