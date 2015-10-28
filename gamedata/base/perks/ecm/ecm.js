// CONSTANTS FOR TARGET RECOGNITION (VALUES FROM TargetInformation!)
var FOF_ANY     = -1;
var FOF_NEUTRAL = 0;
var FOF_FRIEND  = 1;
var FOF_FOE     = 2;

// PERK RANGE
var range = 500;
// MAXIMUM PERIOD BEFORE SWITCHING TARGET
var maxPeriod = 1;
// CURRENT PERIOD
var period = 0;

function add(perk, origin) {
    maxPeriod   = script.getProperties().getFloatValue("period", maxPeriod);
    range       = script.getProperties().getFloatValue("range", range);
}

function remove(perk, origin) {}

function enable(perk, origin) {
    period = 0;
}

function disable(perk, origin) {}

function reset(perk, origin) {
    period = 0;
}

function updateActive(tpf, perk, origin) {
    if (period >= maxPeriod) {
        // CHECK IF WE HAVE SENSORS AND DISABLE PERK IF NOT... (SHOULD NEVER HAPPEN!)
        var sensorControl = origin.getObjectControl(SensorControl);
        if (sensorControl == null) {
            perk.disable(origin);
            return;
        }

        // GET FRIENDLIES WITHIN RANGE
        var friendlyList = sensorControl.getTargetList(range, FOF_FRIEND);
        if (friendlyList.isEmpty()) {
            return;
        }

        // GET LIST OF PEOPLE LOCKING ME
        var lockList = sensorControl.getLockList();
        for (var i = 0; i < lockList.size(); i++) {
            var locker = lockList.get(i);
            // CHECK IF LOCKER HAS SENSORS
            var lockSensors = locker.getObjectControl(SensorControl);
            if (lockSensors != null) {
                // GET A RANDOM FRIENDLY FROM FRIENDLY LIST
                var index = Math.floor(Math.random() * friendlyList.size());
                var friendly = friendlyList.get(index);
                // ASSIGN FRIENDLY AS TARGET (NASTY!)
                lockSensors.targetEnemy(friendly.getObject().getId());
            }
        }
        period = 0;
    } else {
        period += tpf;
    }
}