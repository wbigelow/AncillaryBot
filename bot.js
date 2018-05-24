let Discord = require("discord.js");
let logger = require("winston");
let auth = require("./auth.json");
let config = require("./config.json")
let mysql = require('mysql');

// Configure logger settings
logger.remove(logger.transports.Console);
logger.add(logger.transports.Console, {
    colorize: true
});
logger.level = "debug";

// Anon IDs
let ids = new Array();
let idResetTimer;

// Initialize Discord Bot
let bot = new Discord.Client();
bot.login(auth.token);

// Init db, handle disconnects
let con;
function handleDisconnect() {
    con = mysql.createConnection({
        host: config.host,
        port: config.port,
        user: auth.dbuser,
        password: auth.dbpassword
    });
    con.connect(function(err) {
        if(err) {
            logger.error('error when connecting to db:', err);
            setTimeout(handleDisconnect, 2000);
        }
        con.query("use discord;", function (err, result) {
            if (err) throw err;
        });
    });

    con.on('error', function(err) {
        logger.error('db error', err);
        if(err.code === 'PROTOCOL_CONNECTION_LOST') { 
            handleDisconnect();
        } else {
             throw err;
        }
    });
}
handleDisconnect();


bot.on("ready", function (evt) {
    logger.info("Connected to discord");
});

bot.on("message", message => {
    if(message.content.substring(0, 1) == '>' && !message.author.bot) {
        let cmd = message.content.substring(1, message.content.indexOf(" "));
        if(message.content.indexOf(" ") == -1) {
            cmd = message.content.substring(1);
        }
        switch(cmd) {
            case "dbexec":
                if(message.author.id == "117154757818187783") {
                    dbexec(message.content.substring(message.content.indexOf(" ")), message.channel, true); 
                } else {
                    message.channel.send("```\nError: Unauthorized\n```");
                }
                break;
            case "anon":
                anon(message);
                break;
            case "botsay":
                botsay(message);
                break;
            case "reID":
                reID(message.author.username);
                break;
         }
    }

});

function botsay(message) {
    if(eval(config.anonymous_message_logging)) {
        logger.info("Anon message sent by " + message.author.username + ": " + message.content);
    }
    let content = message.content.substring(message.content.indexOf(" "));
    let channel = message.channel;
    channel.send(content);
    message.delete();
}

function anon(message) {
    if(eval(config.anonymous_message_logging)) {
        logger.info(message.author.username + " sent: " + message.content);
    }
    let content = message.content.substring(message.content.indexOf(" "));
    let anonChannel = bot.channels.get("421077888159449088");
    if(!ids[message.author.username]) {
        let id = Math.floor(Math.random() * 1000);
        ids[message.author.username] = id;
    }
    if(!idResetTimer) {
        idResetTimer = setTimeout(function() {
            ids = new Array();
            idResetTimer = null;
        }, 3600000);
    }
    content = "`" + ids[message.author.username] + "` " + content;
    anonChannel.send(content);
}

function reID(username) {
    let id = Math.floor(Math.random() * 1000);
    ids[username] = id;
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


