// CONSTANTS FOR TARGET RECOGNITION (VALUES FROM TargetInformation!)
var FOF_ANY     = -1;
var FOF_NEUTRAL = 0;
var FOF_FRIEND  = 1;
var FOF_FOE     = 2;

var range       = 3000;

var updateRate  = 1;
var updateTime  = 0;

var targetList;

var factionlist = new Object();

function update(tpf, self) {
    var sensorControl = self.getObjectControl(SensorControl);
    targetList = sensorControl.getTargetList(range, [FOF_FRIEND, FOF_FOE, FOF_NEUTRAL]);
    
    updateTime += tpf;
    
    if (updateTime >= updateRate) {
        var clientCount = 0;
        for (var i = 0; i < targetList.size(); i++) {
            var target = targetList.get(i);
            var object = target.getObject();
            var faction = object.getFaction();
            
            if (faction != null) {
                var key = faction.getId() + "";

                if (object.getClient()) {
                    if (factionlist[key]) {
                        factionlist[key].addClient();
                    } else {
                        factionlist[key] = new FactionPower(faction);
                        factionlist[key].addClient();
                    }
                    clientCount++;
                }            
            }
        }
        
        if (getKeys(factionlist).length == 1) {
            for (var factionId in factionlist) {
                var factionPower = factionlist[factionId];
                if (factionPower.getClientCount() > 0) {
                    factionPower.raisePower();
                    //script.log("faction " + factionId + ": " + factionPower.getPower() + " - " + factionPower.getClientCount());
                } else {
                    factionPower.lowerPower();
                    //script.log("faction " + factionId + ": " + factionPower.getPower() + " - " + factionPower.getClientCount());
                }
            }
        } else {
            for (var factionId in factionlist) {
                var factionPower = factionlist[factionId];
                for (var otherFactionId in factionlist) {
                    factionPower.compareTo(factionlist[otherFactionId], clientCount);
                }
                //script.log("faction " + factionId + ": " + factionPower.getPower() + " - " + factionPower.getClientCount());
            }
        }
        
        for (var factionId in factionlist) {
            var factionPower = factionlist[factionId];
            if (factionPower.getPower() <= 0) {
                delete factionlist[factionId];
            } else if (factionPower.getPower() >= 1) {
                capturePoint(factionPower.getFaction(), self);
            }
            factionPower.reset();
        }
        
        updateTime = 0;
    }
}

function capturePoint(faction, self) {
    if (self.getFaction() == faction) {
        return;
    }
    self.setFaction(faction);
    for (var i = 0; i < targetList.size(); i++) {
        var target = targetList.get(i);
        var object = target.getObject();          
           
        if (!object.getClient()) {
            object.setFaction(faction);
        }
    }
    script.log("point " + self.getId() + " captured by " + faction.getId());
}

function FactionPower(faction) {
    
    var power       = 0;
    var clientCount = 0;
    
    this.reset = function() {
        clientCount = 0;
    }
    
    this.addClient = function() {
        clientCount++;
    }
    
    this.getClientCount = function() {
        return clientCount;
    }
    
    this.getFaction = function() {
        return faction;
    }
              
    this.compareTo = function(otherFaction, fullClientCount) {
        var ownPercent = this.getPercent(fullClientCount);
        var otherPercent = otherFaction.getPercent(fullClientCount);
        if (ownPercent > otherPercent) {
            this.raisePower();
        } else if (ownPercent < otherPercent) {
            this.lowerPower();
        }
    }
       
    this.getPercent = function(fullClientCount) {
        return clientCount / fullClientCount;
    }
       
    this.getPower = function() {
        return power;
    }
       
    this.raisePower = function() {
        power += 0.05;
        if (power > 1) {
            power = 1;
        }
    }
    
    this.lowerPower = function() {
        power -= 0.05;
        if (power < 0) {
            power = 0;
        }
    }
       
}

function getKeys(obj){
   var keys = [];
   for(var key in obj){
      keys.push(key);
   }
   return keys;
}