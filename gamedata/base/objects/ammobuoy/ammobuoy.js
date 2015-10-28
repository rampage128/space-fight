// CONSTANTS FOR TARGET RECOGNITION (VALUES FROM TargetInformation!)
var FOF_ANY     = -1;
var FOF_NEUTRAL = 0;
var FOF_FRIEND  = 1;
var FOF_FOE     = 2;

var range       = 500;
var maxPeriod   = 3;
var period      = 0;

function update(tpf, self) {
    if (period >= maxPeriod ) {
        var sensorControl   = self.getObjectControl(SensorControl);
        var friendlyList    = sensorControl.getTargetList(range, [ FOF_FRIEND ]);

        for (var i = 0; i < friendlyList.size(); i++) {
            var friendly = friendlyList.get(i);
            reloadWeapon(friendly.getObject());
        }
        period = 0;
    }
    period += tpf;
}

function reloadWeapon(target) {
    var weaponControl = target.getObjectControl(WeaponSystemControl);
    if (weaponControl != null) {
        var slots = weaponControl.getSecondarySlots();
        for (var i = 0; i < slots.length; i++) {
            var slot = slots[i];
            if (slot.getUsageCount() > 0) {
                slot.setUsageCount(slot.getUsageCount() - 1);
                return;
            }
        }
    }
}