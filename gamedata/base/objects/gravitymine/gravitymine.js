// CONSTANTS FOR TARGET RECOGNITION (VALUES FROM TargetInformation!)
var FOF_ANY     = -1;
var FOF_NEUTRAL = 0;
var FOF_FRIEND  = 1;
var FOF_FOE     = 2;

var STATE_ARM   = 0;
var STATE_IDLE  = 1;
var STATE_PULL  = 2;
var STATE_PUSH  = 3;

var range       = 800;
var pulltime    = 5;
var armtime     = 6;
var gravity     = 500;
var velocity    = 800;
var disabletime = 10;

var time        = 0;
var state       = STATE_ARM;

var targetList;

function update(tpf, self) {
    if (state == STATE_ARM) {
        wait(tpf);
    } else if (state == STATE_IDLE) {
        scan(self);
    } else if (state == STATE_PULL) {
        pull(self.getPosition(), tpf);
    } else if (state == STATE_PUSH) {
        push(self.getPosition());
    } else {
        script.remove();
    }
}

function wait(tpf) {
    if (time <= armtime) {
        time += tpf;
    } else {
        time = 0;
        state = STATE_IDLE;
    }
}

function scan(self) {
    var sensorControl   = self.getObjectControl(SensorControl);
    
    if (sensorControl != null) {
        targetList = sensorControl.getTargetList(range, [FOF_FRIEND, FOF_FOE, FOF_NEUTRAL]);
        if (targetList != null && !targetList.isEmpty()) {
            state = STATE_PULL;
            script.hidePart("mine");
            script.showPart("pull");
        }
    }
}

function pull(center, tpf) {
    if (time <= pulltime) {
        time += tpf;
        for (var i = 0; i < targetList.size(); i++) {
            var target = targetList.get(i);
            var object = target.getObject();
            if (object.isAlive()) {
                object.disable(pulltime);
                var physics = object.getObjectControl(PhysicsControl);
                if (physics != null) {
                    var localGravity = center.subtract(object.getPosition()).normalizeLocal().multLocal(gravity);
                    physics.setGravity(localGravity);
                }
            }
        }
    } else {
        time = 0;
        state = STATE_PUSH;
        script.hidePart("pull");
        script.showPart("push");
    }
}

function push(center) {
    for (var i = 0; i < targetList.size(); i++) {
        var target = targetList.get(i);
        var object = target.getObject();
        if (object.isAlive()) {
            object.disable(disabletime);
            var physics = object.getObjectControl(PhysicsControl);
            if (physics != null) {
                physics.setGravity(script.getVector3f(0, 0, 0));
            }
        }
        var localVelocity = object.getPosition().subtract(center).normalizeLocal().multLocal(velocity);
        object.setLinearVelocity(localVelocity);
    }
    script.remove();
}