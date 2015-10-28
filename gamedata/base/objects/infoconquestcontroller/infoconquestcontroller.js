// CONSTANTS FOR TARGET RECOGNITION (VALUES FROM TargetInformation!)
var FOF_ANY     = -1;
var FOF_NEUTRAL = 0;
var FOF_FRIEND  = 1;
var FOF_FOE     = 2;

var updateRate  = 1;
var updateTime  = 0;

var ticketCondition = null;

var tension = false;
var ending = false;

function update(tpf, self) {
    
    updateTime += tpf;

    if (ticketCondition == null) {
        script.getMission().addCondition(TicketCondition);
        ticketCondition = script.getMission().getCondition(TicketCondition);
    }

    if (updateTime >= updateRate) {
        checkTickets();
        
        var conquestpoints = script.getMission().getObjectsByType("conquestpoint");
        
        var factionScore = new Object();
        var factions = script.getMission().getFactions()
        for (var i = 0; i < factions.length; i++) {
            var faction = factions[i];
            factionScore[faction.getId() + ""] = 0;
        }
        
        for (var i = 0; i < conquestpoints.length; i++) {
            var conquestpoint = conquestpoints[i];
            var faction = conquestpoint.getFaction();
            if (faction != null) {
                var key = faction.getId() + "";

                if (factionScore[key]) {
                    factionScore[key]++;
                } else {
                    factionScore[key] = 1;
                }
            }
        }

        for (var factionId in factionScore) {
            var factionPower = 1;
            if (factionScore[factionId] > 0) {
                factionPower = 1 - factionScore[factionId] / conquestpoints.length;
            }
            ticketCondition.setFactionTicketDrop(factionId, factionPower);
        }
        
        updateTime = 0;
    }
    
}

function checkTickets() {
    if (!ending) {
        if (tension) {
            script.getMission().setMusicVolume(ticketCondition.tension() / 2);
        } else if (ticketCondition.tension() > 0) {
            tension = true;
            script.getMission().playMusic("tension_2.ogg", ticketCondition.tension() / 2, true);
        }

        if (ticketCondition.survivingFactions() < 2) {
            script.getMission().end();
            ending = true;
        }
    }
}