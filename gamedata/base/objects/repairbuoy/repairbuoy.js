// CONSTANTS FOR TARGET RECOGNITION (VALUES FROM TargetInformation!)
var FOF_ANY     = -1;
var FOF_NEUTRAL = 0;
var FOF_FRIEND  = 1;
var FOF_FOE     = 2;

var range       = 500;
var heal        = 20;

function update(tpf, self) {
    var sensorControl   = self.getObjectControl(SensorControl);
    var friendlyList    = sensorControl.getTargetList(range, [ FOF_FRIEND ]);
    
    for (var i = 0; i < friendlyList.size(); i++) {
        var friendly = friendlyList.get(i);
        var damageControl = friendly.getObject().getObjectControl(DamageControl);
        if (damageControl != null) {
            var currentHP = damageControl.getHullHP() * damageControl.getHull() + heal * tpf;
            if (damageControl.getHull() < 1) {
                var newHull = Math.min(1, currentHP / damageControl.getHullHP());
                damageControl.setHull(newHull);
            }
        }
    }
}