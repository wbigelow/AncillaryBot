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

// ID mapping for Anonymous channel
let anonIDMap = new Map();
// ID Reset timer
setInterval(resetIDs, 86400000);

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
    let anonChannel = bot.channels.get("421077888159449088");
    anonChannel.send("`Ancillary Bot Online`");
});

bot.on("message", message => {
    if(message.content.substring(0, 1) == '>' && !message.author.bot) {
        let cmd = message.content.substring(1, message.content.indexOf(" "));
        if(message.content.indexOf(" ") == -1) {
            cmd = message.content.substring(1);
        }
        switch(cmd) {
            case "eval":
                if(message.author.id == "117154757818187783") {
                    message.channel.send(eval(message.content.substring(message.content.indexOf(" ") + 1)));
                } else {
                    message.channel.send("```\nError: Unauthorized\n```");
                }
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
                reID(message.author);
                break;
            case "message":
                anonMessage(message);
                break;
         }
    }

});

function botsay(message) {
    if(eval(config.anonymous_message_logging)) {
        logger.info(message.author.username + " sent: " + message.content);
    }
    let content = message.content.substring(message.content.indexOf(" "));
    let channel = message.channel;
    channel.send(content);
    if(message.channel.type != "dm") {
        message.delete();
    }
}

function anon(message) {
    if(eval(config.anonymous_message_logging)) {
        logger.info(message.author.username + " sent: " + message.content);
    }
    let content = message.content.substring(message.content.indexOf(" "));
    let anonChannel = bot.channels.get("421077888159449088");
    if(!anonIDMap.has(message.author)) {
        reID(message.author);
    }
    content = "`" + anonIDMap.get(message.author) + "` " + content;
    anonChannel.send(content);
    if(message.channel.type != "dm") {
        message.delete();
    }
}

function reID(author) {
    if(anonIDMap.has(author)) {
        let oldID = anonIDMap.get(author);
        anonIDMap.delete(oldID);
        anonIDMap.delete(author);
    }
    let id = Math.floor(Math.random() * 1000);
    while(anonIDMap.has(id)) {
        id = Math.floor(Math.random() * 1000);
    }
    anonIDMap.set(author, id);
    anonIDMap.set(id, author);
    author.send("You are now sending messages under the ID: `" + id + "`");
}

function resetIDs() {
    let anonChannel = bot.channels.get("421077888159449088");
    anonChannel.send("`Resetting IDs`");
    anonIDMap.clear();
}

function anonMessage(message) {
    let stripped = message.content.substring(message.content.indexOf(" ") + 1);
    let targetID = parseInt(stripped);
    let content = stripped.substring(stripped.indexOf(" ") + 1);
    if(anonIDMap.has(targetID)) {
        if(!anonIDMap.has(message.author)) {
            reID(message.author);
        }
        let target = anonIDMap.get(targetID);
        target.send("`" + anonIDMap.get(message.author) + "` " + content);
        message.author.send("`Message sent`");
    } else {
        message.author.send("`Error: I couldn't find a user with that ID`");
    }
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


