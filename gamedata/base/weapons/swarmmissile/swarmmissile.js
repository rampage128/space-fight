// CONSTANTS FOR TARGET RECOGNITION (VALUES FROM TargetInformation!)
var FOF_ANY     = -1;
var FOF_NEUTRAL = 0;
var FOF_FRIEND  = 1;
var FOF_FOE     = 2;

var range = 500;
var minrange = 50;
var swarmsize = 10;

function update(tpf, weapon) {
    if (weapon.getTarget() != null) {
        if (script.getDistanceToTarget() < range && weapon.getDistance() > minrange) {
            script.destroyWeapon();
        }
    }
}

function destroy(weapon) {
    for (var i = 0; i < swarmsize; i++) {
        script.spawnWeapon("missile", null);
    }
}