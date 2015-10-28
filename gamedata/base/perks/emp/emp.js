// CONSTANTS FOR TARGET RECOGNITION (VALUES FROM TargetInformation!)
var FOF_ANY     = -1;
var FOF_NEUTRAL = 0;
var FOF_FRIEND  = 1;
var FOF_FOE     = 2;

var range = 500;
var time  = 10;
var selftime = 3;

function add(perk, origin) {
    time        = script.getProperties().getFloatValue("time", time);
    selftime    = script.getProperties().getFloatValue("selftime", selftime);
    range       = script.getProperties().getFloatValue("range", range);
}

function remove(perk, origin) {}

function use(perk, origin) {
    var sensorControl = origin.getObjectControl(SensorControl);
    
    if (sensorControl != null) {
        script.playEffect("shieldblast", origin.getPosition(), null, range, 1);
        var targetList    = sensorControl.getTargetList(range, [FOF_FRIEND, FOF_FOE, FOF_NEUTRAL]);
        for (var i = 0; i < targetList.size(); i++) {
            var target = targetList.get(i);
            target.getObject().disable(time);
        }
        origin.disable(selftime);
    }   
}

function reset(perk, origin) {}