let Discord = require("discord.js");
let logger = require("winston");
let auth = require("./auth.json");
let config = require("./config.json")
let mysql = require("mysql");
let { exec } = require("child_process");


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

// Years map
let yearsMap = new Map();
yearsMap.set("freshman", "437466872447893504");
yearsMap.set("sophomore", "452303996640821248");
yearsMap.set("junior", "452304063304957952");
yearsMap.set("senior", "452304274735497228");
yearsMap.set("alum", "451857408692715552");
yearsMap.set("prospective", "414579254211117057");
yearsMap.set("ts", "452344486098632722");


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
});

bot.on("message", message => {
    giveAccess(message);
    if(message.content.substring(0, 1) == '>' && !message.author.bot) {
        let cmd = message.content.substring(1, message.content.indexOf(" ")).toLowerCase();
        if(message.content.indexOf(" ") == -1) {
            cmd = message.content.substring(1).toLowerCase();
        }
        switch(cmd) {
            case "eval":
                if(message.author.id == "117154757818187783") {
                    message.channel.send("`Result:`\n" + eval(message.content.substring(message.content.indexOf(" ") + 1)));
                } else {
                    message.channel.send("```\nError: Unauthorized\n```");
                }
                break;
            case "dbexec":
                if(message.author.id == "117154757818187783") {
                    dbexec(message.content.substring(message.content.indexOf(" ")), message.channel, true); 
                } else {
                    message.channel.send("```\nError: Unauthorized\n```");
                }
                break;
            case "exec":
                if(message.author.id == "117154757818187783") {
                    exec(message.content.substring(message.content.indexOf(" ") + 1), (err, stdout, stderr) => {
                        if (err) {
                            return;
                        }
                        message.channel.send("`Result:`\n" + stdout);
                    });
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
            case "reid":
                reID(message.author);
                break;
            case "message":
                anonMessage(message);
                break;
            case "verify":
                verify(message);
                break;
            case "welcome":
                welcome(message.member);
                break;
            case "year":
                assignYear(message);
                break;
         }
    }

});

bot.on("guildMemberAdd", function (member) {
    welcome(member);
});

function welcome(member) {
    member.addRole("452308369961648128");
    let welcomeChannel = bot.channels.get("452271450653720588"); // Welcome channel ID
    let joinEmbed = new Discord.RichEmbed();
    joinEmbed.setTitle("Welcome " + member.displayName + "!");
    joinEmbed.setColor("#53ff1a");
    joinEmbed.addField("Instructions", "Please do the following to gain access to the server");
    joinEmbed.addField("Step 1", "Read the rules in the #rules channel.")
    joinEmbed.addField("Step 2", "What's your year? Type >year your_year_here to set it." + 
        " (E.g. >year Freshman). Possible answers are: " +
        "Freshman, Sophomore, Junior, Senior, Alum, TS, and Prospective.");
    joinEmbed.addField("Step 3", "Introduce yourself in the #intoductions channel, which you will" +
        " have access to after you complete Step 1 and 2. Once you do this you'll have access to the server!");
    welcomeChannel.send(joinEmbed).then(function(message) {
        message.delete(1800000);
    });
}

function assignYear(message) {
    let year = message.content.substring(message.content.indexOf(" ") + 1);
    year = year.toLowerCase();
    message.delete();
    if(yearsMap.has(year)) {
        message.member.addRole(yearsMap.get(year));
        message.channel.send("Thanks for adding your year! You can now introduce yourself" +
            " in the #introduce-yourself channel.")
            .then(function(message) {
                message.delete(10000);
            });
    } else {
        message.channel.send("Please enter in a valid year from the possible years: " +
            "Freshman, Sophomore, Junior, Senior, Alum, TS, and Prospective")
            .then(function(message) {
                message.delete(10000);
            });
    }
}

function giveAccess(message) {
    if(message.member && message.member.roles.has("452308369961648128") &&
        message.channel.id == 452302353559977984) {
            message.member.removeRole("452308369961648128");
            message.member.addRole("452272203078172692");
    }
    if(message.member && message.member.roles.has("452348969830449152")
        && message.member.roles.has("452272203078172692")) {
        message.member.removeRole("452272203078172692");
    }
}

function botsay(message) {
    if(eval(config.anonymous_message_logging)) {
        logger.info(message.author.username + " sent: " + message.content);
    }
    let content = message.content.substring(message.content.indexOf(" "));
    let channel = message.channel;
    channel.send(removePing(content));
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
    anonChannel.send(removePing(content));
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

function removePing(message) {
    return message.replace(/@/g, "");
}

function verify(message) {
    con.query("SELECT COUNT(*) FROM Verified WHERE name='" + message.author.tag + "';",
    function(err, result) {
        if(err) {
            logger.error(err);
        } else {
            let res = JSON.stringify(result);
            if(res.includes("1")) {
                message.member.addRole("451133170616893441"); // Verified Role
                message.author.send("Thanks for verifying! You are now free to talk in all channels");
            } else {
                message.author.send("Sorry, you haven't verified your UWNetID yet.");
                message.author.send("Please verify at https://students.washington.edu/wbigelow/discordauth");
            }
            message.delete();
        }
    });
}



