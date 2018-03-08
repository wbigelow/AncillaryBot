let Discord = require("discord.js");
let logger = require("winston");
let auth = require("./auth.json");
let mysql = require('mysql');

// Configs
let anonymous_message_logging = false;

// Configure logger settings
logger.remove(logger.transports.Console);
logger.add(logger.transports.Console, {
    colorize: true
});
logger.level = "debug";

// Init db connection
let con = mysql.createConnection({
  host: auth.host,
  port: auth.port,
  user: auth.user,
  password: auth.password
});

// Initialize Discord Bot
let bot = new Discord.Client();
bot.login(auth.token);

// Connect to db
con.connect(function(err) {
    if (err) throw err;
    logger.info("Connected to DB.");
    con.query("use discord;", function (err, result) {
        if (err) throw err;
    });
});


bot.on("ready", function (evt) {
    logger.info("Connected to discord");
});

bot.on("message", message => {
    if (message.content.substring(0, 1) == '>' && !message.author.bot) {
        logger.info(message.content);
        let cmd = message.content.substring(1, message.content.indexOf(" "));
        switch(cmd) {
            case "dbexec":
                if(message.author.id == "117154757818187783") {
                    dbexec(message.content.substring(message.content.indexOf(" ")), message.channel, true); 
                } else {
                    msg.channel.send("```\nError: Unauthorized\n```");
                }
                break;
            case "quote":
                quote(message);
                break;
            case "anon":
                anon(message);
                break;
         }
    }

});

function anon(message) {
    if(anonymous_message_logging) {
        logger.info("Anon message sent by " + message.author.username + ": " + message.content);
    }
    let content = message.content.substring(message.content.indexOf(" "));
    let anonChannel = bot.channels.get("421077888159449088");
    anonChannel.send(content);
    message.delete();
}

function dbexec(message, channel, output) {
    con.query(message, function (err, result) {
        if (err) {
            logger.error(err);
        } else if(output) {
            let res = JSON.stringify(result).split("},{");
            for(let i = 0; i < res.length; i++) {
                channel.send("`" + res[i].replace("[{","").replace("}]","") + "`");
            }
        } else {
            channel.send("Success!");
        }
    });
}

function quote(message) {
    let quote = message.content.substring(message.content.indexOf(" ") + 1);
    let modifier = quote.substring(0, quote.indexOf(" "));
    if(!modifier) {
        modifier = quote;
    }
    quote = sanitize(quote.substring(quote.indexOf(" ") + 1));
    if(modifier === "add") {
        let author = quote.substring(0, quote.indexOf(" - "));
        quote = quote.substring(quote.indexOf(" - ") + 3);
        dbexec("INSERT INTO Quotes(Author, Quote) VALUES (\"" + author + "\",\"" + quote + "\");", message.channel, false);
    } else if(modifier === "get") {
        let author = quote;
        logger.info("author:" + author);
        dbexec("SELECT * FROM Quotes WHERE Author = '" + author + "';", message.channel, true);
    } else if(modifier === "delete") {
        dbexec("DELETE FROM Quotes WHERE ID='"+ quote +"';", channelID, false);
    } else if(modifier === "random") {
        dbexec("SELECT * FROM Quotes ORDER BY RAND() LIMIT 1;", message.channel, true);
    } else {
        message.channel.send("Usage:\n\n`>quote add Author - Quote`\n`>quote get Author`\n`>quote delete ID`\n`>quote random`");
    }
}

function sanitize(string) {
    return string.replace("\\","").replace("\"","\\\"").replace("'","\\'");
}

